# <img src="./noodles.svg" alt="noodles" style="width: 31px;"/> Noodle
[![Build Status](https://travis-ci.org/nolia/Noodle.svg?branch=master)](https://travis-ci.org/nolia/Noodle)   [![](https://jitpack.io/v/nolia/Noodle.svg)](https://jitpack.io/#nolia/Noodle)

Noodle is a simple object storage for Android.

### Download

Get it from JitPack.

Step 1. Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
   ...
    maven { url 'https://jitpack.io' }
  }
}
```

Step 2. Add the dependency
```
dependencies {
  compile 'com.github.nolia:Noodle:v0.1'
}
```

### Usage

It's really simple: no schema, no relations, no consistency rules, no thread-contained objects.
Just create Noodle instance and register types you want to store:

```java
class Book {
  long id;
  String title;
  String author;

  public Book(String title, String author) {
    this.title = title;
    this.author = author;
  }
}

Noodle noodle = new Noodle(context)
  .registerType(Book.class, Description.of(Book.class)
    .withIdField("id")
    .build()
  );

```

Note: no annotations needed.

### Basic operations
Noodle allows you to *list*, *put*, *delete*  and *get* (by id) your objects.

```java
collection = noodle.collectionOf(Book.class);

Book book = new Book("I Robot", "Isaac Asimov");

// Get all.
List<Book> list = collection.all().now();

collection.put(book).now();
// Now, book object has updated id.

// Update:
book.title = "I, Robot";
collection.put(book).now();

// Delete:
collection.delete(book.id).now();

```

### Storage
Noodle stores all the data as byte arrays. By default it converts classes using Gson. But you can bring your own converter and store objects the way you want.


### Still to be done:
 * Id generation strategy
 * Simple annotation processing for entities ids
 * Rx support
 * Indexes (maybe)
 * Documentation :)