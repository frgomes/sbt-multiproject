sbt-multiproject
================

> A surprisingly simple approach for multiple SBT configurations and multiple projects

Credits
-------

Brought to you by [Mathminds](https://mathminds.io)

Motivation
----------

Multiple configurations are useful for organizing integration tests,
functional tests, acceptance tests, performance tests and so on.

Multiple projects are needed whenever your application depends on
multiple JVMs, or multiple Scala versions, or multiple versions of
libraries or environments.

But how we can achieve both aims without pulling our hairs?

There are a number of SBT plugins around but they sometimes don't play
well with IDEs or they present some undesirable or incomplete
behavior, which you only discover too late, after investing a lot of
time on them.

Features
--------

Multiple configurations
+++++++++++++++++++++++

Multiple configurations similar to IntegrationTest are defined and
wired properly for you. They just run as you would expect.

  * FunctionalTest
  * AcceptanceTest
  * PerformanceTest
  
You can also define your own configurations and wire then onto your
projects as easy as the provided configuration are wired for you.

Multiple projects
+++++++++++++++++

> Everything is easy, after you know how to do it.

Creating multiple projects which work properly is actually easy, after
you understand how to do it. The big advantage of doing it yourself is
that you have full command of it and you have total flexibility.

We provide detailed documentation, so that even if you are not
acquainted to SBT too much... yeah... you will get it working as you
would expect.

Interdependencies between configurations
++++++++++++++++++++++++++++++++++++++++

When you are creating your test cases, many times you would like to
create your own utilities and resources for unit tests and your own
utilities and resources for integration tests. In particular, in many
situations, you would like to employ resources from one configuration
onto another.

This is autowired for you. You just need to define your configurations
and all your *Test* sources and resources will be visible from
*IntegrationTest* and all others. Also, all your sources and resources
defined for *IntegrationTest* will be visible from all other
configurations.

 * Test -> IntegrationTest, FunctionalTest, AcceptanceTest,
   PerformanceTest;

 * IntegrationTest -> FunctionalTest, AcceptanceTest,
   PerformanceTest;

No external plugins
+++++++++++++++++++

The solution is self contained. No external plugins. No magic popping
up from nowhere. Everything is written in Scala, sits inside your
project folder and, actually... is shorter than you may think.

Clear documentation
+++++++++++++++++++

The key for clear documentation is writing it in a way that I would
like to learn such thing, imagining that I know zero about that.
