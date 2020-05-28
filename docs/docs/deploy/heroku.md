---
title: Deploy to Heroku
summary: A walkthrough of deploying to heroku
authors:
    - Stephen van Beek
date: 2020-05-27
---

# Deploying to Heroku

[Heroku](https://www.heroku.com) is a PaaS product that makes deploying your application easy. With it's generous free tier it's a perfect way to kick off a new project.

Springboard works pretty well with Heroku and benefits a fair bit from a number of Heroku add-ons:

* [mailgun](https://mailgun.com) as an SMTP server to send emails;
* [sentry](https://sentry.io/welcome/) for error-tracking;
* [logdna](https://logdna.com/) for log aggregation; and 
* [hosted postgresql](https://www.heroku.com/postgres) as a data store.

You can check the other add-ons heroku comes with out of the box [here](https://elements.heroku.com/addons).

## Minimum Viable Deployment

There are a couple of ways that we can deploy to Heroku - they maintain excellent docs on this [here](https://devcenter.heroku.com/categories/deployment). 
The deployment mechanism we'll be focusing on is the github integration. We'll be building out a production-ready pipeline here. 
We'll connect our app to a springboard based github repo and deploy the application with a database.

One of the primary benefits to working in Springboard is simplicity of building/deploying. It's a single, stateless JVM 
instance that's connected to a database. Most of the tricky first-time setup has already been done in the gradle build scripts. 
As such, anything that can deploy a spring boot project can deploy a springboard project without difficulty. 

As it happens, Heroku has terrific integration with Spring Boot so deploying a live, production-ready application is pretty simple.   

I'm going to presume you've signed up to a Heroku account, if you haven't [then do so](https://signup.heroku.com/), login to the Heroku dashboard and get ready to go. 

### Step 1: Create a Heroku App

If you've just signed up to Heroku you'll be presented with an empty dashboard. On this page, click the `New` button and select `App`.

[![heroku empty dashboard](/assets/images/heroku-deploy/heroku-empty-dashboard.png)](/assets/images/heroku-deploy/heroku-empty-dashboard.png)

This will take you to the create application page:

[![heroku create a new app page](/assets/images/heroku-deploy/heroku-create-app.png)](/assets/images/heroku-deploy/heroku-create-app.png)

When there fill in the details for your application and hit `Create App`. This will take you on to your new app page:

[![heroku newly created app](/assets/images/heroku-deploy/heroku-created-app.png)](/assets/images/heroku-deploy/heroku-created-app.png)

Hooray! An app is born!

### Step 2: Create a pipeline and deploy

Now, our app doesn't actually do anything yet. Let's fix that.

You'll be prompted to add this app to a pipeline at the top of the page. Click to create a new pipeline and fill in the details:

[![heroku pipeline creation](/assets/images/heroku-deploy/heroku-create-pipeline.png)](/assets/images/heroku-deploy/heroku-create-pipeline.png)

This will forward you on to the newly created pipeline:

[![heroku newly created pipeline](/assets/images/heroku-deploy/heroku-pipeline-created.png)](/assets/images/heroku-deploy/heroku-pipeline-created.png)

When here, click the prompt to `Connect to Github`. If you don't see this prompt then navigate to the `Settings` tab. Once on this page, search for your GitHub repo then connect.

[![heroku connected pipeline](/assets/images/heroku-deploy/heroku-pipeline-connected.png)](/assets/images/heroku-deploy/heroku-pipeline-connected.png)

While you're here, I'd really recommend enabling [review apps](https://devcenter.heroku.com/articles/github-integration-review-apps). You can see the settings we recommend in the image below:

[![heroku connected pipelione enable review apps](/assets/images/heroku-deploy/heroku-review-settings.png)](/assets/images/heroku-deploy/heroku-review.settings.png)

They'll give you a great way to interact with pull request changes before merging. Springboard comes with an `app.json` file configured that specifies the required postgres database for your review apps. If you'd like to add other addons for each review app, then you can do so by modifying `app.json`. Heroku will respect the changes on the next deploy.

### Step 3: Enable automatic deploys

Navigate back to the `Pipeline` tab to see the new setup.

[![springboard pipeline with review apps](/assets/images/heroku-deploy/heroku-pipeline-with-review.png)](/assets/images/heroku-deploy/heroku-pipeline-with-review.png)

We'll add a staging environment here later. For now, let's enable automatic deploys. Click the menu toggle button on the production app.

[![springboard pipeline with deploy](/assets/images/heroku-deploy/heroku-pipeline-deploy-settings.png)](/assets/images/heroku-deploy/heroku-pipeline-deploy-settings.png)

And click `Configure automatic deploys...`. You'll be presented with some options. We recommend deploying master only after CI passes. 

[![springboard pipeline with deploy](/assets/images/heroku-deploy/heroku-automatic-deploys.png)](/assets/images/heroku-deploy/heroku-automatic-deploys.png)

And we're set! This means that the next time we merge into `master` we'll see our application automatically updated!

### Step 4: Making a change

Let's make a tiny change. I've added a new empty line between two tags in `src/webapp/src/templates/pages/index.peb`. You could do something more dramatic here of course.

Commit this change on a feature branch `feature/example` and push to github. Now go to your repo in the browser and you'll 
be prompted to open a pull-request. Click the `Compare & pull request...` button, fill out the form on this page and then 
create the pull request. 

[![github pull request with CI running](/assets/images/heroku-deploy/github-pull-request-ci-running.png)](/assets/images/heroku-deploy/github-pull-request-ci-running.png)

Alright! Now go make a cup of coffee while the CI build runs. Springboard has a longer build than a base Spring Boot project, 
but we promise it's worth it. Right now GitHub is running unit, integration and feature tests on the back-end and has 
kicked off Cypress to test your front-end. Once it's done you should see something like this:

[![pull request with deployment](/assets/images/heroku-deploy/github-pull-request-review-created.png)](/assets/images/heroku-deploy/github-pull-request-review-created.png)

Click the `View dpeloyment` button and... Ta-da!

[![pull request with deployment](/assets/images/heroku-deploy/review-app-deployed.png)](/assets/images/heroku-deploy/review-app-deployed.png)

Our review app is live! This has it's own Heroku Postgres instance spun up so we can play around with it without impacting a shared database.

### Step 5: Merging to master and deploying to production

Now that we've done some QA on our review app, we'll merge the pull request and deploy to production. Normally, this is 
where a teammate would review our change, but for now we can safely click the `Merge` button and carry on solo.

Upon merging the pull request, the review app instance will be spun down - saving us some money/free dyno hours. At this point you can head back to the heroku pipeline to view the deploy logs.

[![heroku deployment in progress](/assets/images/heroku-deploy/heroku-deploy-logs.png)](/assets/images/heroku-deploy/heroku-deploy-logs.png)

Give it a couple of minutes to run and your app will be deployed! You can also check in on the deployment in the GitHub environment screen, by clicking the `environments` button on your repo's home page.

[![github env is pending](/assets/images/heroku-deploy/github-env-pending.png)](/assets/images/heroku-deploy/github-env-pending.png)

Once the app has been deployed this will change to read `Deployed`. 

Click the `View deployment` button... and it's broken! This is because Heroku doesn't respect the `app.json` file's addons in production. I don't fully understand why they made this choice, but let's fix it.

### Step 6: Spinning up the production database

Navigate over to the application in Heroku and go to the resources tab:

[![heroku app resources](/assets/images/heroku-deploy/heroku-resources.png)](/assets/images/heroku-deploy/heroku-resources.png)

And provision a free database:

[![Adding the heroku postgres addon](/assets/images/heroku-deploy/heroku-postgres-free.png)](/assets/images/heroku-deploy/heroku-postgres-free.png)

Now click `More` at the top right and `Restart all dynos`. Now - after the restart completes - we can try clicking `Open App` again...

[![Deployment was successful](/assets/images/heroku-deploy/deploy-success.png)](/assets/images/heroku-deploy/deploy-success.png)

Success! Our app is now live in production!

### Step 7: reflect on the result

To conclude, we now have:

* an automated process that runs all of our tests for a pull request;
* on test success deploys the pull request code to a unique, isolated environment;
* on merge deletes the review application automatically; and
* deploys the change to production.

This is incredibly powerful. Whether you're working solo or in a team, I think having a way to easily review and trial changes before 
letting a customer get access to them is the way forward. There's more yet to do though! Continue on below to allow your application to send email,
report errors to sentry and to have a long-lived staging environment for QA.

## Adding mailgun

TODO

## Adding Staging

TODO

## Adding Sentry

TODO

## Adding LogDNA
