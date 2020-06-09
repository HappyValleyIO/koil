---
title: Welcome to SpringBoard
summary: An introduction to the what and why of Springboard
authors:
    - Stephen van Beek
date: 2020-06-09
---


# Welcome to Springboard

## What is it?
Springboard is an opinionated, full-stack template for building server-rendered applications in Kotlin without sacrificing the developer experience provided by modern front-end tooling. 

Springboard comes out-of-the-box with everything you need to launch your rich web application in hours rather than weeks. We include:

* authentication workflows (including password reset emails, registration and more);
* live-reload in development; 
* CSS pre-processors;
* Typescript;
* a core set of common StimulusJS controllers required for basic reactivity;
* rich example-driven documentation (everything from making a change, to writing the test, to running in production);
* production deployment niceties (including caching of static assets with hashed names);
* and more. 

If there's something you think we're missing, then open an issue on the github.

## Who is this for?
When starting [Happy Valley IO](https://www.happyvalley.io/) there were definite gaps in our knowledge. Coming from corporate backgrounds, we hadn't really needed to deal with a bunch of the fixed up-front setup that working on a small team (or as a solo developer) requires. I mean, how often do you have to re-build the password reset emails?

We based the tech choices we've made in Springboard around our imagined typical JVM developer who's trying to launch a SaaS product in their spare time, or starting their own company. A developer who is very comfortable working in a standard Spring Boot setup and knows enough front-end to build a great user experience without necessarily being an expert in how modern front-end build pipelines work. That's not to say that it won't work well in a larger org, but we're not building with them in mind.

This is a tool for users who love Spring Boot, who love working on the JVM, who love simple production infrastructure, and who want to deploy their product **now**. This is a tool for Java/Kotlin developers who look at Ruby on Rails devs with a little bit of envy. We're not optimizing for technical purity here, but for Getting Shit Done.

Effectively, this is what we wish we'd had available when we left corporate life.

## Why is it better than the alternatives?
While we've historically embraced the vogue in tech (spinning up JSON API's that powered SPA front-ends), we've come to the conclusion that we can do something simpler without giving up user or developer experience. To be honest, we've found it's easier to have fast, bug-free front-end code without large javascript bundles.

We believe that we're cherry-picking the best of two worlds in Springboard:

* we have the JVM as our main runtime platform, giving us great performance, great stability and a terrific eco-system to leverage;
* we have a Node.js based build pipeline for our front-end, allowing us to leverage great tools like Typescript, SASS and best-in-class asset bundling to have the best developer experience possible.

In this way, we can take advantage of the best parts of building a traditional monolithic MPA (initial load performance, amazing editor integration for view-models, easy deployment, etc.) without sacrificing the awesome tools that we've loved using while working on SPA's in Vue or React.

What's more, there no reason that you can't use these frameworks in Springboard. We just think you'll be happy to kick things off without them :).

## Why are you releasing this for free?
Through open source all things are possible - so jot that down. We've benefited tremendously from open source, and we want to give back.

From a business perspective, we will only benefit from marketing of our core consulting business with the free version but we are planning on releasing a premium licensed version with additional features later this year.
