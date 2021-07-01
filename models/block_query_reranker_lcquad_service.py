import os

import tensorflow as tf


from bert.tokenization import FullTokenizer, validate_case_matches_checkpoint

from flask import Flask, request, jsonify, json
from bert.modeling import BertConfig
from bert.run_classifier import InputExample, convert_single_example, model_fn_builder
# from run_classifier_with_tfhub import create_tokenizer_from_hub_module, model_fn_builder


class Service(object):
    def __init__(self):

        self.label_list = ['0', '1']
        self.vocab_file = './data/config/vocab_new.txt'
        self.bert_config_file = './data/config/bert_config.json'
        self.init_checkpoint = './checkpoints/block_query_reranking_lcquad'
        self.max_seq_length = 128
        self.do_lower_case = True
        self.train_batch_size = 32
        self.eval_batch_size = 8
        self.predict_batch_size = 8

        # by default first run
        self.closed = False
        self.first_run = True

        # mention and relation to predict
        self.mention = ["A"]
        self.relation = ["B"]
        self.batch_size = 1

        # predictions
        self.predictions = None

        self.tokenizer = FullTokenizer(vocab_file=self.vocab_file)
        self.estimator = self.get_estimator()

    def get_estimator(self):
        # check if lower_case matches the model
        validate_case_matches_checkpoint(
            self.do_lower_case, self.init_checkpoint)
        # load bert config file
        bert_config = BertConfig.from_json_file(self.bert_config_file)
        # check max_seq_length
        if self.max_seq_length > bert_config.max_position_embeddings:
            raise ValueError(
                "Cannot use sequence length %d because the BERT model "
                "was only trained up to sequence length %d" %
                (self.max_seq_length, bert_config.max_position_embeddings))

        # gpu config
        config = tf.ConfigProto()
        config.gpu_options.allow_growth = True
        #config.gpu_options.per_process_gpu_memory_fraction = 0.5

        # run_config
        run_config = tf.contrib.tpu.RunConfig(
            cluster=None,
            model_dir=self.init_checkpoint,
            session_config=config,
            save_checkpoints_steps=1000)

        # bert model
        model_fn = model_fn_builder(
            bert_config=bert_config,
            num_labels=len(self.label_list),
            init_checkpoint=self.init_checkpoint,
            learning_rate=0.00005,
            num_train_steps=1000,
            num_warmup_steps=100,
            use_one_hot_embeddings=False,
            use_tpu=False)

        # estimator
        estimator = tf.contrib.tpu.TPUEstimator(
            use_tpu=False,
            model_fn=model_fn,
            config=run_config,
            warm_start_from=self.init_checkpoint,
            train_batch_size=self.train_batch_size,
            eval_batch_size=self.eval_batch_size,
            predict_batch_size=self.predict_batch_size
        )
        return estimator

    def get_feature(self, text_a, text_b, index=1):
        """单文本的feature生成"""
        guid = text_a+","+text_b
        example = InputExample(guid, text_a, text_b, "0")
        feature = convert_single_example(
            index, example, self.label_list, self.max_seq_length, self.tokenizer)
        return feature.input_ids, feature.input_mask, feature.segment_ids, feature.label_id

    def get_feature_batch(self, text_a_batch, text_b_batch, batch_size):
        """"一个batch的feature生成"""
        input_ids = []
        input_mask = []
        segment_ids = []
        label_ids = []

        for i in range(batch_size):
            features = self.get_feature(
                text_a_batch[i], text_b_batch[i], index=i)
            input_ids.append(features[0])
            input_mask.append(features[1])
            segment_ids.append(features[2])
            label_ids.append(features[3])

        return input_ids, input_mask, segment_ids, label_ids

    def create_generator(self):
        """构建生成器"""
        while not self.closed:
            features = self.get_feature_batch(
                self.mention, self.relation, self.batch_size)
            # print("features:"+str(features))
            # name_to_features = {
            #     "input_ids": tf.FixedLenFeature([self.max_seq_length], tf.int64),
            #     "input_mask": tf.FixedLenFeature([self.max_seq_length], tf.int64),
            #     "segment_ids": tf.FixedLenFeature([self.max_seq_length], tf.int64),
            #     "label_ids": tf.FixedLenFeature([], tf.int64),
            # }
            # feature_dict = tf.parse_single_example(features,name_to_features)
            feature_dict = {
                "input_ids": features[0],
                "input_mask": features[1],
                "segment_ids": features[2],
                "label_ids": features[3]
            }
            yield feature_dict
            # yield dict(zip(("input_ids", "input_mask", "segment_ids", "label_ids"), zip(*features)))

    def input_fn_builder(self, params):
        # batch_size=params["size"]
        """对单独预测数据进行创建，不基于文件数据"""
        # item = self.create_generator.next()
        dataset = tf.data.Dataset.from_generator(
            self.create_generator,
            output_types={
                'input_ids': tf.int32,
                'input_mask': tf.int32,
                'segment_ids': tf.int32,
                'label_ids': tf.int32
            },
            output_shapes={
                'input_ids': tf.TensorShape([None, 128]),
                'input_mask': tf.TensorShape([None, 128]),
                'segment_ids': tf.TensorShape([None, 128]),
                'label_ids': tf.TensorShape([None]),
            }
        )

        return dataset

    def predict(self, text_a, text_b, batch_size):
        self.mention = text_a
        self.relation = text_b
        self.batch_size = batch_size
        if self.first_run:
            self.predictions = self.estimator.predict(
                input_fn=self.input_fn_builder, yield_single_examples=False)
            self.first_run = False

        probabilities = next(self.predictions)['probabilities']
        # print(probabilities)
        # return probabilities[0][1]
        # print(type(probabilities))
        return probabilities[:, 1]

    def close(self):
        self.closed = True


app = Flask(__name__)


@ app.route('/relation_detection', methods=['POST'])
def relation_detection_service():
    """
    params['question']: natural language question
    params['label']: candidate predicate label
    """
    if request.method == 'POST':
        decoded_data = request.data.decode('utf-8')
        params = json.loads(decoded_data)
        rel_labels = params['labels']
        question = params['question']
        batch_size = len(rel_labels)
        questions = [question for i in range(len(rel_labels))]
        #questions = [question*len(rel_lables)]

        if batch_size==0:
            return jsonify({'detection_res':[]})
        res = relation_detection(questions, rel_labels, batch_size, service)
        print("res:"+str(res))
        return jsonify({'detection_res': res.tolist()})


def relation_detection(text_a: list, text_b: list, batch_size: int, service: Service):
    print("[INFO] text_a: " + str(text_a) + " , text_b: " +
          str(text_b)+"batch_size:"+str(batch_size))
    return service.predict(text_a, text_b, batch_size)


if __name__ == "__main__":

    # not using GPU
    os.environ['CUDA_VISIBLE_DEVICES'] = ''
    service = Service()

    # set the log level
    tf.logging.set_verbosity(tf.logging.INFO)

    # initialize
    result = relation_detection(
        ['owns the websites for writes', 'owns the websites for writes'], ['owner', 'result'], 2, service)
    print('result:' + str(result))
    # service.close()

    # 0.0.0.0 makes it externally visible
    app.run(host="0.0.0.0", port=5683, debug=False)
