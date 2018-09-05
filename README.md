### DSL
:paw_prints: Started to creating :books: Domain Specific Language
:sparkles: Stay tuned :octocat:

##### Dependencies

[![Gradle](https://img.shields.io/badge/gradle-4.5-orange.svg?longCache=true&style=plastic)](https://gradle.org/)
[![Groovy](https://img.shields.io/badge/groovy-2.4.11-orange.svg?longCache=true&style=plastic)](http://groovy-lang.org/)
[![Java](https://img.shields.io/badge/java-1.8.0__171-orange.svg?longCache=true&style=plastic)](https://www.java.com)


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
mailserver{
    sethost 'smtp.gmail.com'
    login 'bhanuchander210@gmail.com','*******'

    sendmsg{

        to 'example@gmail.com'
        cc 'any@gmail.com'
        bcc 'any@gmail.com'
        subject 'DSL-EMAIL UTIL'
        body'''
                    ....content ..
            '''
        attach 'dsl.groovy'

    }
}
``` 
**Examples**
 
 Find more examples on the directory `dsl/examples`.

**Release Notes:**

**Version 1.1**

- `jsonstudy`
   - Parsing json file
   - Printing json tree
   - Node selectable 2D Conversion
   - Node selectable Iteration
   
- `excelstudy`
    - Column selection
    - 2D Conversion
    - sheet list
    
**Version 1.0**

- `filestudy`
    - Make pattern - Converting from uneven file to even file by making `pattern`.
    - Normal filtering.
    - Picking up words by index and occurrence. 
- `email`
    - Sending mail from code.
    - Attaching files and body format (`text`,`html`).