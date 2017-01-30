# Noodle

Noodle is a simple object storage for Android.


It's really simple: no schema, no relations, no consistency rules, no thread-contained objects.
And it's also easy to use, create Noodle instance and register types you want to store:

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
Noodle allows you to *put*, *delete*  and *get* (by id) your objects.

```java
collection = noodle.collection(Book.class);

Book book = new Book("I Robot", "Isaac Asimov");

// Get all.
collection.all().now(); // Returns List<Book>.

collection.put(book).now();
// Now, book object has updated id.

// Update:
book.title = "I, Robot";
collection.put(book).now();

// Delete:
collection.delete(book.id).now();

```

### Storage
Noodle stores all the data as byte arrays. By default it converts classes using Gson.