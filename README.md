# EDGQA

Codes for "EDG-based Question Decomposition for Complex Question Answering over Knowledge Bases".

## Requirements

- JDK 1.8.0
- Maven

## Program Arguments

### EDGQA Arguments

```text
-d --dataset: 'lc-quad', 'qald-9', 'qald-8', or 'qald-7'
-tr --train: 'true' for training set, 'false' for test set
-r --run: 'autotest', 'single', or 'serial_number'
-uc --use_cache: 'true' for using linking cache, 'false' otherwise
-cc --create_cache: 'true' for creating linking cache, 'false' otherwise
-gll --global_linking: 'true' for using global linking, 'false' otherwise
-lll --local_linking: 'true' for using local linking, 'false' otherwise
-qd --question_decomposition: 'true' for using EDG to decompose the question
-rr --reranking: 'true' for re-ranking by EDG block, 'false' otherwise
```

### PointerNetwork Arguments

```text
--dataset: 'lc-quad', 'qald-9'
```

## Resources

- [PKUmod paraphrase dict](https://github.com/pkumod/Paraphrase/blob/master/dic.txt)
- [QALD-6](https://qald.aksw.org/index.php?x=home&q=6)
- [QALD-7](https://project-hobbit.eu/challenges/qald2017/)
- [QALD-8](https://project-hobbit.eu/challenges/qald-8-challenge/)
- [QALD-9](http://2018.nliwod.org/challenge)
- [syntax tree tool]: http://mshang.ca/syntree/

