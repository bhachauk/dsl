### DSL
:paw_prints: Started to creating :books: Domain Specific Language
:sparkles: Stay tuned :octocat:


##### Overview

Domain specific language is really helpful to understand the code and reduces 
the time taken to modify. Normal java code really needs more code lines and makes
this the code complex to understand. Implementing the `JAVA` code as DSL with groovy
really helps a lot.

This `dsl` repo is a simple template to taste the dsl features, It can be
implemented over `JAVA CLI` with enhanced features.


##### Getting Started:

`Gradle` is used to build this repo. To build use command,

```groovy
gradle cD
```

After the successful build, you can find the `dsl` inside the `build` directory.
Use the `run.groovy` inside the `build/dsl/bin` to run your DSL scripts.

**To Run :**

```text
cd build/dsl/bin
groovy run myscript.groovy
```

**Example :** `myscript.groovy`

```groovy

``` 
 


**Release Notes:**

**Version 1.0**

- filestudy
    - Features
        - Make pattern - Converting from uneven file to even file by making `pattern`.
        - Normal filtering.
        - Picking up words by index and occurrence. 
- email
    - Features
        - Sending mail from code.
        - Attaching files and body format (`text`,`html`).