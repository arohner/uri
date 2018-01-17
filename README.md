# url [![Travis CI status](https://secure.travis-ci.org/cemerick/url.png)](http://travis-ci.org/#!/cemerick/url/builds)

This is a library that makes working with URLs in Clojure and ClojureScript a
little more pleasant.

Fork of [cemerick/url], only difference is this uses java.net.URI rather than java.net.URL

Q: Why do I care about this difference?

A:

````
Since the URL class has an openConnection method, the URL class checks to make sure that Java knows how to open a connection of the correct protocol. Without a URLStreamHandler for that protocol, Java refuses to create a URL to save you from failure when you try to call openConnection.
```

https://stackoverflow.com/questions/2406518/why-does-javas-url-class-not-recognize-certain-protocols

https://github.com/cemerick/url/issues/3

## "Installation"

url is available in Clojars. Add this `:dependency` to your Leiningen
`project.clj`:

```clojure
[com.arohner/uri "0.1.2"]
```

Or, add this to your Maven project's `pom.xml`:

```xml
<repository>
  <id>clojars</id>
  <url>http://clojars.org/repo</url>
</repository>

<dependency>
  <groupId>com.arohner</groupId>
  <artifactId>uri</artifactId>
  <version>0.1.2</version>
</dependency>
```

## Usage

The `arohner.uri/uri` function returns an instance of the
`arohner.uri.URI` record type that allows you to easily work with each
datum within the provided URI:

```clojure
=> (require '[cemerick.url :refer (url url-encode)])
nil
=> (-> (url "https://api.stripe.com/v1/charges")
     (assoc :username "vtUQeOtUnYr7PGCLQ96Ul4zqpDUO4sOE")
     str)
"https://vtUQeOtUnYr7PGCLQ96Ul4zqpDUO4sOE:@api.stripe.com/v1/charges"
```

`url` will also accept additional paths to be resolved against the path
in the base URL:

```clojure
=> (url "https://api.twitter.com/")
#cemerick.url.URL{:protocol "https", :username nil, :password nil,
                  :host "api.twitter.com", :port -1, :path "/", :query nil,
                  :anchor nil}
=> (url "https://api.twitter.com/" "1" "users" "profile_image" "cemerick")
#cemerick.url.URL{:protocol "https", :username nil, :password nil,
                  :host "api.twitter.com", :port -1,
                  :path "/1/users/profile_image/cemerick", :query nil, :anchor nil}
=> (str *1)
"https://api.twitter.com/1/users/profile_image/cemerick"
=> (str (url "https://api.twitter.com/1/users/profile_image/cemerick" "../../lookup.json"))
"https://api.twitter.com/1/users/lookup.json"
```

The `:query` slot can be a string or a map of params:

```clojure
=> (str (assoc *3 :query {:a 5 :b 6}))
"https://api.twitter.com/1/users/profile_image/cemerick?a=5&b=6"
```

Note that `url` does not perform any url-encoding of paths.  Use
`cemerick.url/url-encode` to url-encode any paths/path components prior
to passing them to `url`.  e.g.:

```clojure
=> (def download-root "http://foo.com/dl")
#'cemerick.test-url/download-root
=> (str (url download-root "/"))
"http://foo.com/"
=> (str (url download-root (url-encode "/")))
"http://foo.com/dl/%2F"
=> (str (url download-root (url-encode "/logical/file/path")))
"http://foo.com/dl/%2Flogical%2Ffile%2Fpath"
```
## Need Help?

Ping `cemerick` on freenode irc or
[twitter](http://twitter.com/cemerick) if you have questions or would
like to contribute patches.

## License

Copyright ©2012 [Chas Emerick](http://cemerick.com) and other contributors

Distributed under the Eclipse Public License, the same as Clojure.
Please see the `epl-v10.html` file at the top level of this repo.
