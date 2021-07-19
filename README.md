CloudSlang
==========

[![Join the chat at https://gitter.im/CloudSlang/cloud-slang](https://badges.gitter.im/CloudSlang/cloud-slang.svg)](https://gitter.im/CloudSlang/cloud-slang?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

CloudSlang is a YAML based language for writing human-readable workflows for the CloudSlang Orchestration Engine .
This project includes the CLI to trigger flows.

[![Build Status](https://travis-ci.org/CloudSlang/cloud-slang.svg?branch=master)](https://travis-ci.org/CloudSlang/cloud-slang)
[![Coverage Status](https://coveralls.io/repos/CloudSlang/cloud-slang/badge.svg?branch=master)](https://coveralls.io/r/CloudSlang/cloud-slang?branch=master)
[![Code Climate](https://codeclimate.com/github/CloudSlang/cloud-slang/badges/gpa.svg)](https://codeclimate.com/github/CloudSlang/cloud-slang)

#### Getting started:

###### Pre-Requisite: Java JRE = 8

1. Download the CloudSlang CLI file named cslang-cli-with-content:
    + [Stable release](https://github.com/CloudSlang/cloud-slang/releases/latest)
    + [Latest snapshot](https://github.com/CloudSlang/cloud-slang/releases/)
2. Extract it.
3. Go to the folder /cslang/bin/
4. Run the executable :
  - For Windows : cslang.bat
  - For Linux : bash cslang
5. Run a simple example print text flow:  run --f ../content/io/cloudslang/base/print/print_text.sl --i text=first_flow

#### Documentation

All documentation is available on the [CloudSlang website](http://www.cloudslang.io/#/docs).

#### What's New

See what's new [here](CHANGELOG.md).

#### Get Involved

Read our contributing guide [here](CONTRIBUTING.md).

Contact us at support@cloudslang.io.

#### Building and Testing from Source

###### Pre-Requisites:

1. Maven version >= 3.0.3
2. Java JDK version = 8

###### Steps

1. ```git clone``` the source code
2. ```mvn clean install```
3. Run the CLI executable from cloudslang-cli\target\cslang\bin

### CloudSlang Docker Image
Just use:

``` docker pull cloudslang/cloudslang ```

And run it using:

``` docker run -it cloudslang/cloudslang ```

### CloudSlang npm Package
###### cslang-cli
> The CloudSlang command line interface.

Install this globally and you'll have access to the `cslang` command anywhere on your system.

```shell
npm install -g cloudslang-cli
```

Now you can just use the `cslang` command anywhere
```shell
cslang
```

###### Pre-Requisites
Node.js & Java installed.

cslang-cli page in the [npm repository](https://www.npmjs.com/package/cslang-cli).
