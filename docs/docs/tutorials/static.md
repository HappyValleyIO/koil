---
title: How to add a static page
summary: A walkthrough of two methods to add a static page
authors:
    - Stephen van Beek
date: 2020-05-13
---

# Adding a static-ish page

koil has a pretty funky setup. We're using node to build web things that are then rendered/served with Spring Boot. 
As a result there are two ways to think about adding a static - or mostly static - page.

We can either:
* add a document as a fully static document in HTML; or
* we can add a template that doesn't rely on a model and render it using Spring.

While the former is a little easier, I'd recommend always going for the latter. It lets you use the same template engine, 
which means you get to leverage the same layout as the rest of your site. It also allows you to leverage the live reload 
functionality when working in the `dev` profile.

## Adding a new template to the project

### Project structure

Almost all front-end assets are held in the `src/webapp` directory. If you dig into that directory you'll see a setup like so:

<div style="width:80%; margin:0 auto">
    <img src="/assets/images/webapp-directory.png" alt="webapp directory overview image">
</div>

So underneath the `src/webapp` directory we have a pretty standard NodeJs project layout. The `src` directory inside this 
project is what we're interested in right now.

Let's run through what these directories represent:

* **js:** the javascript and typescript we're using in our project;
* **styles:** the CSS/SASS assets for the project;
* **templates:** the [pebble](https://pebbletemplates.io/) templates that are used by Spring at runtime to render a page.

We'll loop back around to use the js and styles in a little bit, but for now, let's dig in to the templates directory further:

<div style="width:80%; margin:0 auto">
    <img src="/assets/images/templates-expanded.png" alt="webapp templates directory image">
</div>

Inside the `templates` directory we have some subdirectories. 

* **layouts:** the core layouts used by our site to share core functionality and - well - layout;
* **pages:** by convention, we put standalone pages in the pages directory. We recommend structuring the directories roughly like the URL path these things will end up on, but there's no reason that you have to;
* **components:** snippets and pebble macros that are shared across the project. These *generally* aren't rendered solo, but we do sometimes do so for e.g. a modal;
* **errors:** in this directory we put standard templates for Spring to fall back on when it encounters certain HTTP error codes.

### Adding a new page

Now that we've walked through where everything lives, let's add a "Hello World" page.

First we create a `hello.peb` file in the `pages` directory, with content:

```jinja
{% extends 'layouts/base' %}

{% block 'body' %}
<h1>Hello World!</h1>
{% endblock %}
```

Here, we're using the `layouts/base.peb` layout and passing in our title element to the `body` block. 
If you go to the base layout you'll see a snippet like this:

```jinja
<body>
{% block body %}{% endblock %}
</body>
``` 

This means that anything that extends the base layout can add a [block](https://pebbletemplates.io/wiki/tag/block/) to 
insert its own content into the layout.

Now, let's add a Spring Controller endpoint to actually serve this page. In the `PublicController` class (src/main/kotlin/org/koil/public/PublicController.kt), 
add a new controller endpoint that renders this template:

```kotlin
  @GetMapping("/hello")
  fun hello(): ModelAndView {
    return ModelAndView("pages/hello")
  }
```

And navigate to your running application at (http://localhost:8080/hello) to see the page. It works!

This is all you have to do to have a page load. Next up is some style.

### Adding some styles

By default, koil uses [bulma](https://bulma.io/). Bulma is a css framework that gives a bunch of nice utilities out of the box.

Let's make use of some of these bulma CSS classes. Our `hello.peb` file now looks like:

```jinja
{% extends 'layouts/base' %}

{% block 'body' %}
<div class="container">
    <div class="content">
        <h1 class="title is-size-1 has-text-centered">Hello World!</h1>
    </div>
</div>
{% endblock %}
```

If you reload the page you'll see something quite different! Remember, you'll have to rebuild the project for Spring to pick up the template update (Ctrl+F9 in IntelliJ).

To see how custom styles work, let's take a look at `base.peb`. We can see in this file that we pull in another stylesheet:

```html
    <link rel="stylesheet" type="text/css" href="/styles/dashboard.scss" data-turbolinks-track="reload">
```

Any time we have a href start with "/" in koil, it's in reference to the base webapp source at `src/webapp/src/`.

If we navigate to `src/webapp/src/styles/dashboard.scss` we'll see that we have a file with a bunch of styles already defined. 
Let's add our own to the bottom:

```scss
.hello-title {
  color: red !important;
}
```

And apply it in our `hello.peb` file:

```jinja
{% extends 'layouts/base' %}

{% block 'body' %}
<div class="container">
    <div class="content">
        <h1 class="title hello-title is-size-1 has-text-centered">Hello World!</h1>
    </div>
</div>
{% endblock %}
```

You can see we've added the `hello-title` class to our `h1` element. And it works! The title is now red. We have the ability to add custom styles.

## Custom js

Now let's walk through adding a little javascript. We want to add a button that will change the color of the title between black and red.

First, let's take a look at `base.peb` again to see if there's something that helps us here. We notice the line:

```html
    <script type="text/javascript" src="/js/index.cjs" defer data-turbolinks-track="reload"></script>
```

Navigating to this file like before, we see that it's a fairly standard ES5 file pulling in the dependencies we need. The one we're interested in is `import './application.js`.

Looking at this file, we'll see a bunch of `Controller` classes being imported and added to a [StimulusJS](https://stimulusjs.org/) application. 
If you've never used stimulus before then I'd recommend going through their [handbook](https://stimulusjs.org/handbook/introduction).

Alright, let's add our own `Controller` to these. Create a file called `hello_controller.js` in the `controllers` directory. In this file, we'll add:

```js
import { Controller } from "stimulus";

export default class HelloController extends Controller {
  static get targets() {
    return ['title'];
  }

  toggleStyle() {
    this.titleTarget.classList.toggle('hello-title')
  }
}
```

And hook it into the application in `application.js`, by adding an import at the top:

```js
import HelloController from "./controllers/hello_controller";
```

And a registration at the bottom:

```js
application.register("hello", HelloController);
```

We'll also need to update our `hello.peb` file to reflect this:

```jinja
{% extends 'layouts/base' %}

{% block 'body' %}
<div class="container">
    <div class="content" data-controller="hello">
        <h1 class="title hello-title is-size-1 has-text-centered" data-target="hello.title">Hello World!</h1>
        <button type="button" class="button" data-action="click->hello#toggleStyle">Toggle</button>
    </div>
</div>
{% endblock %}
```

Re-build the project and... success! When we click the button it toggles the `.hello-title` style so that it changes colour.

There are a couple of changes in `hello.peb` that I'd like to draw your attention to:

* We've added a `data-controller="hello"` attribute to the `div` on the second line. This will let Stimulus know that it should create a `HelloController` instance and connect it to this DOM element.
* We've added a `data-target="hello.title"` attribute to the `h1` tag. This tells Stimulus to treat this DOM element as the `this.titleTarget` field we made use of in our controller.
* We've added a `data-action="click-hello#toggleStyle"` on our `button`. This tells Stimulus to listen for a `click` event on this button and to execute the `toggleStyle` method in our `HelloController` when it does. 
 
Once again, this isn't intended to be a tutorial around Stimulus (we wrote one [here](https://happyvalley.dev/building-a-simple-static-site-with-parcel-and-stimulus/)), so we're not going to go into it any further right now.
 
## Summary

So we've worked through the process of adding a new page with styles and js to the project. In the next tutorial, 
we'll add a form to this page and hook it in to the database. 
