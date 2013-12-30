PouchDroid
===========

**Version 0.1.0**

![logo][]

Effortlessly sync your data across multiple Android devices, using [PouchDB][] and [CouchDB][].

|Table of Contents|
|-----------------|
| [Introduction](#introduction) |
| [Basic Usage](#basic-usage) |
| [Q&amp;A](#qa) |
| [What's supported?](#whats-supported) |
| [Tutorials](#tutorials) |
| [License](#license) |
| [Author](#author) |

Introduction
-------------

### What is this?

PouchDroid is an Android adapter for [PouchDB][] written in Java.  It offers a simple key-value store, backed by SQLite, that can automatically sync with a remote [CouchDB][] via HTTP/HTTPS.

### Why do I care?

Syncing is hard.  You don't want to have to manage revisions, conflicts, and incremental changes yourself.  CouchDB/PouchDB will handle all that junk for you, so you can devote your brain cells to other problems.

Also, ORM is hard.  So instead of forcing you to write SQL or add ```@AnnoyingAnnotations```, PouchDroid lets you persist your plain old Java objects as JSON. And it uses the same API as PouchDB, which is the same API as CouchDB.  Fewer APIs == fewer brain cells wasted.

### Why Couch/Pouch?

CouchDB is awesome.  If you use its [built-in user authentication][couchsecurity], you can write Ajax apps with barely any server code (or none at all!).

PouchDB is awesome too.  It runs cross-browser, and it offers automagical two-way sync between the client and server.

Basic Usage
----------

Your Activity extends ```PouchDroidActivity```:

```java
public class MyActivity extends PouchDroidActivity {
  /* ... */
  public void onPouchDroidReady(PouchDroid pouchDroid) {
    // do stuff
  }
}
```

Your POJO extends ```PouchDocument```:

```java
public class Meme extends PouchDocument {
  String name;
  String description;
  // getters and setters (must have a bare constructor)
}
```

You create a database and add POJOs:

```java
PouchDB<Meme> pouch = PouchDB.newPouchDB(Meme.class, pouchDroid, "memes.db");
pouch.post(new Meme("Doge", "Much database, very JSON. Wow."));
pouch.post(new Meme("AYB", "All your sync are belong to PouchDB."));
```

Then you set up continuous bidirectional sync with CouchDB:

```java
pouchDB.replicateTo("http://user:password@mysite.com:5984/mydb", true);
pouchDB.replicateFrom("http://user:password@mysite.com:5984/mydb", true)
```

You check the remote CouchDB:

```
curl 'http://user:password@mysite.com:5984/_all_docs?include_docs=true' | python -mjson.tool
```

And it returns:
```json
{
    "offset": 0, 
    "rows": [
        {
            "doc": {
                "_id": "e01532d5cb2765bc0b80dcbe687474c9", 
                "_rev": "2-b1344c55cdf88a537c6698f7f81745a1", 
                "description": "Much database, very JSON. Wow.", 
                "name": "Doge"
            }, 
            "id": "e01532d5cb2765bc0b80dcbe687474c9", 
            "key": "e01532d5cb2765bc0b80dcbe687474c9", 
            "value": {
                "rev": "2-b1344c55cdf88a537c6698f7f81745a1"
            }
        }, 
        {
            "doc": {
                "_id": "e01532d5cb2765bc0b80dcbe68747ede", 
                "_rev": "2-f6d6f0e06d6912c6bd40329c0bcf604f", 
                "description": "All your sync are belong to PouchDB.", 
                "name": "AYB"
            }, 
            "id": "e01532d5cb2765bc0b80dcbe68747ede", 
            "key": "e01532d5cb2765bc0b80dcbe68747ede", 
            "value": {
                "rev": "2-f6d6f0e06d6912c6bd40329c0bcf604f"
            }
        }
    ], 
    "total_rows": 2
}
```



You'll never have to touch ```SQLiteOpenHelper``` again.  And if your user
opens the app on another device, their data is already waiting for them.

Q&amp;A
-------------

### How does this work?

PouchDB is a JavaScript library.  So, PouchDroid creates an invisible WebView, which it basically uses as a JavaScript interpreter in order to run PouchDB.

To improve performance, PouchDB's calls to WebSQL are rerouted to the native Android SQLite API, and calls to XMLHttpRequest are rerouted to the Apache HttpClient API.  [Jackson][] is used for JSON serialization/deserialization.

### Isn't the performance terrible?

Not really.  Since most of the heavy lifting is done in SQLite/HTTP, relatively little code is executed on the UI thread.  PouchDroid even manages to run on my vintage HTC Magic (2008) rocking Android 2.1 Eclair.  And with Chrome included as the standard WebView in 4.4 KitKat, it's only gonna get faster.

### Why not run on Cordova/PhoneGap?

I figured it would be overkill to include all the Cordova libraries.  Plus, the Android WebView normally runs WebSQL queries on the UI thread, meaning that even a spinning progress bar would stutter whenever you're doing database operations.

Also, getting Ajax to work would require tedious configuration of CORS/JSONP to get around web security, whereas PouchDroid works on a freshly-installed CouchDB.

As it is, PouchDroid doesn't have any external dependencies, other than PouchDB and Jackson.  The APK clocks in at about 700K (400K with ProGuard).

### But I already store user data in SQLite!

PouchDroid includes a small utility called ```PouchDroidMigrationTask```, which can migrate your existing SQLite tables into a pretty reasonable key-value format.  So, if you don't want to dive head-first into Pouch, you can use it purely for one-way sync to CouchDB.

### What are the limitations?

1. PouchDroid needs a WebView in order to run JavaScript.  Hence, you can't use it in a background Service, and it does consume UI cycles.  For small databases, though, you probably won't notice.
2. Actually, that's the only limitation.

Android 2.1 (API level 7) and up is supported.

What's supported?
------------

| - | Pouch API                  |
|-----------|----------------------------|
| ```[x]``` | ```create/destroy```       |
| ```[x]``` | ```put/post/get/remove```  |
| ```[x]``` | ```bulkDocs/allDocs```  |
| ```[ ]``` | ```changes```  |
| ```[x]``` | ```replicate```  |
| ```[ ]``` | ```views```  |
| ```[ ]``` | ```info```  |
| ```[ ]``` | ```compact```  |
| ```[ ]``` | ```revsDiff```  |

Tutorials
----------

* [Getting started with PouchDroid](//github.com/nolanlawson/PouchDroid/wiki/Getting-Started)

License
----------

Apache 2.0

The Android robot is reproduced or modified from work created and shared by Google and used according to terms described in the Creative Commons 3.0 Attribution License.

Author
--------
Nolan Lawson

[1]: https://github.com/pgsqlite/PG-SQLitePlugin-Android-2013.09
[2]: http://guide.couchdb.org/draft/conflicts.html
[3]: http://tritarget.org/blog/2012/11/28/the-pyramid-of-doom-a-javascript-style-trap/]
[pouchdb]: http://pouchdb.com/
[couchdb]: http://couchdb.apache.org/
[jackson]: http://jackson.codehaus.org/
[couchsecurity]: http://guide.couchdb.org/draft/security.html
[logo]: https://raw.github.com/nolanlawson/PouchDroid/master/gimp/pouchdroid_logo.png
