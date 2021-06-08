# EDGQA

Codes for "EDG-based Question Decomposition for Complex Question Answering over Knowledge Bases".

## Requirements

- JDK 1.8.0
- Maven

## Program Arguments

### EDGQA Arguments

Program arguments are defined in `src/main/java/cn/edu/nju/ws/edgqa/utils/QAArgs.java`.

Running settings for [Intellij IDEA 2019.3 above versions](https://www.jetbrains.com/idea/) are stored in `EDGQA/.run`.

Run `cn/edu/nju/ws/edgqa/handler/EDGQA.java` by following CLI arguments:

```text
-d --dataset: 'lc-quad', 'qald-9'
-tr --train: 'true' for training set, 'false' for test set
-r --run: 'autotest', 'single', or 'serial_number'
-uc --use_cache: 'true' for using linking cache, 'false' otherwise
-cc --create_cache: 'true' for creating linking cache, 'false' otherwise
-gll --global_linking: 'true' for using global linking, 'false' otherwise
-lll --local_linking: 'true' for using local linking, 'false' otherwise
-qd --question_decomposition: 'true' for using EDG to decompose the question
-rr --reranking: 'true' for re-ranking by EDG block, 'false' otherwise
```

Because the linking tool consumes a lot of time, caching the linking results of the test queries helps improve the speed
of the test. The cache needs to be built the first time the QA is run and is available when it is run again. Use the
arguments `use_cache` and `create_cache` above to set the cache tool.

### PointerNetwork Arguments

Run `cn/edu/nju/ws/edgqa/handler/PointerNetworkQA.java` by following CLI arguments:

```text
--dataset: 'lc-quad', 'qald-9'
```

## Resources

- [PKUmod paraphrase dict](https://github.com/pkumod/Paraphrase/blob/master/dic.txt)
- [QALD-6](https://qald.aksw.org/index.php?x=home&q=6)
- [QALD-7](https://project-hobbit.eu/challenges/qald2017/)
- [QALD-8](https://project-hobbit.eu/challenges/qald-8-challe3nge/)
- [QALD-9](http://2018.nliwod.org/challenge)
- [syntax treeNode tool](http://mshang.ca/syntree/)

## Contact

Feel free to create a GitHub Issue or send an e-mail.
