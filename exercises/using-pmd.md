# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset. Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false negative). Explain why you would not solve this issue.

## Answer
After installing the pmd, I I've chosen [Apache Commons Collections](https://github.com/apache/commons-collections) to analyse it.

Using this command, I analyzed the repo, using the general java rules to produce the result in html format
```bash
run.sh pmd -d commons-collections/ -R rulesets/java/quickstart.xml -f html -r report.html
```

Extrat from result :


### True Positive issue
The issue name is: **Do not use if statements that are always true or always false**
Issue found in the MapUtilsTest.java class, line 1147

The part of code:
```java
assertEquals(val.intValue(), MapUtils.getNumber(in, "noKey", key -> {
    if (true) {
        return val;
    }
    return null;
}).intValue(), 0);
```
In the example above, we use an unconditional if statement, and the condition is always true.

I think it's better to refactor this part, because we know that we can optimize the complexity of program.


### False positive issue
The issue name is: **Do not add empty strings**
Error found in the PatriciaTrieTest.java
The part of code:
```java
final char char_b = 'b'; // 1100010
final PatriciaTrie<String> trie = new PatriciaTrie<>();
final String prefixString = "" + char_b;
final String longerString = prefixString + u8000;
```
Here we see that adding an empty string was to convert the char into a string using concatenation.


