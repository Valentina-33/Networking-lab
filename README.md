# Networking Lab: Sequential HTTP Mini Web Application

## 1. Project title and description

**Networking Lab, Part 2: From a Minimal HTTP Server to a Web Application on AWS.**

This project takes a minimal, socket-based Java HTTP server (`ServerSocket` and `Socket`, no
framework) and turns it into a small but complete web application. It serves an HTML page, a
JavaScript client, and images as static resources, and it exposes four hardcoded JSON services
(greeting, square, server time, health). The browser talks to it asynchronously, so the page never
reloads. But the server itself stays strictly **sequential**: it accepts and fully handles one
connection before it accepts the next one.

The point of this lab is not scalability itself, but understanding it. Before adding threads,
pools, or load balancers, you first need to see, with your own server, what "one request at a
time" really means, and how a single HTML page quietly turns into several separate HTTP requests
(the document, the script, the images, the JSON calls). Concurrency and distribution are out of
scope on purpose. They are the next step, and they only make sense once you have seen the limits
of this simpler version.

The author deployed the packaged jar to AWS EC2 by hand, following the steps described in Section
10 of this README. Deployment is not automated by this codebase, and it is not part of the source
tree.

## 2. System metaphor and architecture

**System metaphor: a post office with a single clerk and one service window.**

Picture a small post office. There is exactly one clerk (**the Java server**) and one service
window (**the listening socket**, bound to a single TCP port). Customers (**browsers**) walk up
one at a time. The clerk always finishes the current customer completely, handing over every
stamp, package, and receipt, before calling the next person in line. The clerk never serves two
customers at once. That would need a second clerk, and on purpose, this post office does not have
one yet.

When a customer asks for "the standard package" (**a GET request for `/`**), the clerk hands over
a folder with a form (**`index.html`**), a short instruction sheet (**`app.js`**), and a couple of
sample photos (**the images**). The customer reads the instructions right there at the counter,
and instead of getting back in line to talk to the clerk in person again, uses a small request-slip
system (JavaScript's `fetch`) to send follow-up requests through a mail slot (an **asynchronous
HTTP call**): asking for a greeting, a squared number, or the time on the clock behind the counter
(**the hardcoded services**). The clerk still handles each slip one at a time, in order, just like
every other customer. The mail slot only means the customer does not have to stand frozen at the
window while waiting. It does **not** give the clerk a second pair of hands.

Everything the clerk can hand out without thinking twice, the form, the instructions, the sample
photos, lives in one drawer under the counter (**the `public/` resources bundled in the jar**).
The clerk checks that drawer by name only, and flatly refuses to go digging through any other
drawer in the building if a request slip tries to name one (**the path traversal check**). A
short, fixed list of "special requests" the clerk knows by heart (**the hardcoded routes**:
greeting, square, time, health) is handled from memory instead of from the drawer. There is no
general "request router" employee here, on purpose, because the lab wants you to see the plain
`if` statements that decide behavior, not a framework that hides them.

Finally, the whole post office building was later picked up and placed inside **one AWS EC2
instance**. The building's front door lock (**the security group**) decides who can even walk in:
SSH admin access limited to the author's own IP, and the service window's port opened for lab
testing. Moving buildings changed where the post office physically is and who can knock on the
door. It changed nothing about how the one clerk inside handles customers.

```mermaid
flowchart LR
    subgraph Browser["Browser (client)"]
        UI["index.html + style.css"]
        JS["app.js (fetch, async)"]
    end

    subgraph EC2["AWS EC2 instance"]
        SG["Security Group\n(SSH: author IP only\nApp port: lab-scoped)"]
        subgraph JVM["Java process"]
            SS["ServerSocket\n(one listening port)"]
            LOOP["Sequential accept() loop"]
            CH["ConnectionHandler\n(parse, route, respond, close)"]
            ROUTER["RequestRouter\n(explicit if/else on path)"]
            STATIC["StaticResourceHandler\n+ PathNormalizer"]
            SVC["Hardcoded services:\nGreeting / Square / Time / Health"]
            RES[("public/ resources\nbundled in the jar")]
        end
    end

    UI -->|"user action"| JS
    JS -->|"HTTP GET (async)"| SG
    SG --> SS --> LOOP --> CH --> ROUTER
    ROUTER -->|"/api/*"| SVC
    ROUTER -->|"everything else"| STATIC --> RES
    SVC -->|"JSON"| CH
    STATIC -->|"bytes + content type"| CH
    CH -->|"HTTP response"| JS
    JS -->|"update result/error area"| UI
```

Here is what each part of the code does, matched to the metaphor:

| Component | File | Responsibility |
|---|---|---|
| Browser client | [`index.html`](src/main/resources/public/index.html), [`style.css`](src/main/resources/public/style.css) | The counter and the customer's own paperwork: page structure and layout. |
| Asynchronous client | [`app.js`](src/main/resources/public/app.js) | The request-slip system. Fires `fetch()` calls, shows loading, result, and error states, and never reloads the page. |
| Server entry point | [`NetworkingServer`](src/main/java/edu/eci/arsw/networking/NetworkingServer.java) | Opens the one service window (`ServerSocket`) and runs the sequential accept loop. |
| Connection handler | [`ConnectionHandler`](src/main/java/edu/eci/arsw/networking/ConnectionHandler.java) | The clerk serving exactly one customer, start to finish, per connection. |
| Router | [`RequestRouter`](src/main/java/edu/eci/arsw/networking/RequestRouter.java) | The clerk's memorized list of special requests: plain `if`/`else` on the path, no framework. |
| Static resources | [`StaticResourceHandler`](src/main/java/edu/eci/arsw/networking/StaticResourceHandler.java), [`PathNormalizer`](src/main/java/edu/eci/arsw/networking/util/PathNormalizer.java) | The drawer under the counter, plus the rule that no request slip can point outside it. |
| Hardcoded services | [`services/`](src/main/java/edu/eci/arsw/networking/services) | Greeting, Square, ServerTime, Health: each one a small, independently testable class. |
| HTTP plumbing | [`http/`](src/main/java/edu/eci/arsw/networking/http) | Request parsing, response building, and writing bytes back to the socket. |

## 3. Design decisions

- **The server stays sequential on purpose.** `NetworkingServer` runs one `while` loop around
  `serverSocket.accept()`, and the next `accept()` only happens after `ConnectionHandler.handle()`
  returns. No `Thread`, `ExecutorService`, or thread pool appears anywhere in the request path.
  This is the lab's teaching goal in Section 6.2: make the "one capacity limit" real and
  observable before trying to fix it.
- **Routes are hardcoded on purpose.** `RequestRouter` is a handful of `if` statements comparing
  `request.path()` against four fixed strings. There is no reflection-based dispatcher, no
  annotation scanning, no dependency injection container. The lab asks for the path-to-behavior
  mechanism to stay visible.
- **Content types are picked by file extension**, using a fixed `Map<String,String>` in
  `ContentTypes`. Every resource, text or binary, is read into a `byte[]`, and the
  `Content-Length` header is computed from `bytes.length`, never from `String#length()`. That way
  multi-byte UTF-8 text and binary images both round-trip correctly.
- **Unsafe paths are rejected before any lookup happens.** `PathNormalizer` walks the requested
  path segment by segment and rejects the request the moment a `..` would go above the public
  root. It never builds a path that could escape the resources area and hand it to the
  classloader. This check runs for every static request, no matter how the resource is stored.
- **Static resources are bundled inside the jar** (`src/main/resources/public`, loaded through the
  classloader) instead of being read from an external folder. That way the whole application,
  page, script, images, and server, ships and deploys as **one artifact**: copy a single `.jar` to
  EC2 and run it.
- **The browser client is asynchronous.** Every form submit and button click is handled with
  `event.preventDefault()` plus `fetch()`, so the page can show a loading state, keep the rest of
  the UI interactive, and tell a network failure apart from a valid HTTP error response, all
  without implying that the server itself became concurrent. Section 6.2 exists specifically to
  make that distinction visible.
- **Untrusted input is always escaped before it goes into a JSON body.** `JsonUtil.escape` and
  `JsonUtil.quote` are the only places that build a JSON string from user-supplied data (a name
  from the query string, an echoed invalid value). That way a value containing `"`, `\`, or
  control characters can never close the string early or sneak in an extra field.
- **No dependency beyond the JDK and JUnit.** The server, router, services, and JSON building use
  only `java.net`, `java.io`, and `java.util`, matching the lab's "no framework" spirit and
  keeping the deployable artifact self-contained.

## 4. Project structure

```
Networking/
├── pom.xml                          # Maven build descriptor (Java 17, JUnit 5)
├── .gitignore
├── README.md
├── deploy/
│   ├── networking-lab.service.template   # systemd unit template for EC2 (placeholders only)
│   └── run.sh                            # convenience local/EC2 run script
├── docs/                                 # evidence screenshots (see docs/README.md)
├── src/
│   ├── main/
│   │   ├── java/edu/eci/arsw/networking/
│   │   │   ├── NetworkingServer.java     # entry point: sequential accept() loop
│   │   │   ├── ConnectionHandler.java    # handles exactly one connection
│   │   │   ├── RequestRouter.java        # hardcoded path to handler mapping
│   │   │   ├── StaticResourceHandler.java
│   │   │   ├── http/                     # HttpRequest/HttpResponse plus parser/writer
│   │   │   ├── services/                 # Greeting, Square, ServerTime, Health, Slow
│   │   │   ├── util/                     # ContentTypes, PathNormalizer, JsonUtil
│   │   │   └── part1/                    # preliminary exercises, not graded here (Section 15)
│   │   └── resources/public/             # HTML, CSS, JS, images served by the server
│   └── test/
│       └── java/edu/eci/arsw/networking/ # JUnit 5 tests, mirroring the main package layout
```

Application code lives under `src/main/java`. Public static resources live under
`src/main/resources/public` (Maven's usual resources directory, bundled into the jar). Every test
lives separately under `src/test/java`, mirroring the package it exercises. The `part1/` package
holds standalone exercises from the earlier networking guide (Section 15) and is not part of the
Part 2 application itself.

## 5. Prerequisites

- **Java 17** or later (a JDK, not just a JRE, since the build compiles from source).
- **Apache Maven 3.9+**.
- A modern browser, for local and manual testing of the asynchronous client.
- For the AWS section only: an approved AWS account and region, plus an SSH client, or the AWS
  Session Manager or EC2 Instance Connect plugin, depending on what your course allows.

## 6. Installation and build

```bash
git clone <this-repository-url>
cd Networking
mvn test        # compiles and runs the full test suite
mvn package      # produces target/networking-lab-1.0.0.jar (runs the tests first)
```

`mvn package` produces a single runnable jar at `target/networking-lab-1.0.0.jar`, with the public
resources already bundled inside it. That jar is the whole deployable artifact.

## 7. How to run locally

```bash
java -jar target/networking-lab-1.0.0.jar
```

- **Port:** defaults to `35000`. You can change it with an environment variable or a system
  property:
  ```bash
  PORT=8080 java -jar target/networking-lab-1.0.0.jar
  # or
  java -Dport=8080 -jar target/networking-lab-1.0.0.jar
  ```
- **Browser address:** open `http://localhost:35000/` (or whatever port you chose).
- **Shutdown:** press `Ctrl+C` in the terminal running the server. A JVM shutdown hook closes the
  listening socket cleanly. That is the same mechanism `systemctl stop` uses once the application
  runs as a managed service on EC2 (Section 7.4 of the lab).

`deploy/run.sh [port]` wraps the same command for convenience, locally or on the EC2 instance.

## 8. How to use the application

The home page (`/`) offers three main actions, all handled asynchronously, so the page never
reloads:

| Action | Special URL | Required input | Success response | Invalid input |
|---|---|---|---|---|
| Get greeting | `GET /api/greeting?name=<value>` | Non-empty `name` | `200` JSON: `{"name": "...", "message": "Hello, ...!"}` | `400` JSON `{"error": "..."}` when `name` is missing or blank |
| Get square | `GET /api/square?value=<value>` | Numeric `value` | `200` JSON: `{"input": <n>, "square": <n squared>}` | `400` JSON `{"error": "..."}` when `value` is missing, non-numeric, or infinite |
| Get server time | `GET /api/time` | none | `200` JSON: `{"serverTime": "<ISO-8601 offset date-time>"}` | none |
| Health check | `GET /api/health` | none | `200` JSON: `{"status": "UP"}` | none |

Only `GET` is accepted on every route. Any other method returns `405 Method Not Allowed` with an
`Allow: GET` header. A request for a static resource that does not exist returns `404 Not Found`.
A request that tries to read outside the public resources area (a `..` traversal attempt, however
it is encoded) is rejected with `400 Bad Request` before any file lookup even happens.

**One extra route that is not required: `GET /api/slow?seconds=<n>`.** This is not one of the four
services Section 4 asks for. It exists only so the "open two browser windows" experiment from
Section 6.2 can be reproduced by clicking a button, instead of having to invent a slow request some
other way. It blocks the single server thread for `seconds` (0 to 30, defaults to 5) before
returning `{"sleptSeconds": <n>, "finishedAt": "<ISO-8601 offset date-time>"}`. See Section 9 for
how to use it to watch the sequential limitation in action.

## 9. How to run the tests

```bash
mvn test
```

The suite (80 tests, including `part1/EchoIntegrationTest`) covers three layers:

- **Unit tests** for the pieces that need to be exactly right on their own: `PathNormalizerTest`
  (traversal rejection), `ContentTypesTest` (extension to MIME type mapping), `JsonUtilTest`
  (escaping, including an injection-attempt case), `HttpRequestParserTest` (request-line and
  query-string parsing), `HttpResponseWriterTest` (byte-accurate `Content-Length`, the `Allow`
  header on 405), and one test class per hardcoded service (`GreetingServiceTest`,
  `SquareServiceTest`, `ServerTimeServiceTest`, `HealthServiceTest`, `SlowServiceTest`).
- **Component tests** for `RequestRouter` and `StaticResourceHandler`, checking the routing
  decisions and the static-file and traversal behavior without opening a socket.
- **End-to-end tests** in `NetworkingServerIntegrationTest`, which start the real server on a free
  port and drive it with real HTTP requests (`java.net.http.HttpClient`, plus one raw `Socket`
  request that sends an un-normalized `..` path byte for byte). This includes ten requests in a
  row against the same running process, matching the lab's functional test matrix (Section 6.1).

**Manual checks** (done by the author before every deployment): load the home page in a browser
and confirm the network tab shows separate successful requests for the HTML, the script, and both
images, each with the right content type; submit each form and confirm the result or error area
updates without a page reload.

**Watching Section 6.2 with two browser windows:**
1. Open the home page in two separate browser windows, side by side.
2. In the first window, use the "Slow service" card to trigger `GET /api/slow?seconds=8`.
3. Right away, in the second window, ask for the greeting or the server time.
4. Watch the second window's request sit as `(pending)` in the Network tab. It only finishes once
   the first window's slow request is done, even though the second window's page stayed fully
   interactive the whole time. That gap is the sequential server at work. The interactive second
   window is the asynchronous client. Those are two different things, and it is easy to mix them
   up.

## 10. AWS deployment

The application was deployed to a single AWS EC2 instance, using the account, region, Linux image,
and instance size approved by the course.

1. **Prepare the artifact locally:** run `mvn package`, then confirm that `java -jar
   target/networking-lab-1.0.0.jar` serves the page and all four services on your own machine.
2. **Launch the instance:** one Linux EC2 instance in the default VPC and public subnet, tagged
   with a clear lab name, reached through the connection method the course approved (Session
   Manager, EC2 Instance Connect, or SSH limited to the author's own public IP).
3. **Security group:** inbound SSH (port 22) limited to the author's IP only, plus one custom TCP
   inbound rule for the application port, scoped to whatever range the instructor allowed for
   classroom testing.
4. **Install the runtime:** installed a matching JDK (Java 17 or newer) using the instance
   image's package manager, for example `dnf install java-17-amazon-corretto` on Amazon Linux.
5. **Transfer the artifact:** copied `target/networking-lab-1.0.0.jar` (the single deployable
   artifact, resources included) to the instance using the approved connection method's file
   transfer, for example `scp`.
6. **Run it as a managed service:** installed `deploy/networking-lab.service.template` as a
   systemd unit, with its placeholders filled in for the instance's real user, working directory,
   JDK path, and port (see the template's header for the exact commands). This way the process
   starts on boot, restarts if it fails, logs to a known file, and stops cleanly with
   `systemctl stop`.
7. **Verify:** ran `curl http://localhost:<port>/api/health` from inside the instance first, then
   opened `http://<ec2-public-address>:<port>/` from a local browser to confirm the page, script,
   images, and all three dynamic services work through the public address.

No private key, AWS credential, instance ID, or public IP is stored in this repository. See
Section 11 for where the matching evidence is kept.

## 11. Evidence and results

Screenshots live under [`docs/`](docs/README.md), which also lists what each one should show and
the lab section it backs up. Every image below is embedded straight from that folder, so each one
appears here as soon as its file is added. See `docs/README.md` for the full checklist and some
capture tips.

**Local execution**

![Server console and baseline browser response](docs/local-baseline.png)
![Several consecutive requests handled by one running process](docs/local-sequential.png)

**Static resources and controlled errors**

![Network tab: separate requests for HTML, CSS, JS, and both images](docs/network-static.png)
![404, 405, and a rejected path-traversal attempt](docs/response-errors.png)

**Hardcoded services**

![Valid and invalid responses for each service](docs/services.png)

**Asynchronous client**

![Successful request updating the result area without a page reload](docs/async-client.png)
![Invalid input rendered as a friendly error message](docs/async-client-error.png)

**Sequential limitation (Section 6.2)**

![Second window pending while /api/slow runs in the first](docs/sequential-two-windows.png)

**AWS deployment**

![EC2 security group inbound rules](docs/security-group.png)
![Health service verified from inside the instance](docs/ec2-health.png)
![Application served from the EC2 public address](docs/ec2-running.png)
![systemd service reported active and enabled](docs/ec2-systemd.png)
![Application still responding after the admin session was closed](docs/ec2-logout.png)

**AWS cleanup (Section 10)**

![EC2 instance terminated](docs/ec2-terminated.png)

## 12. Known limitations

- The server is **strictly sequential**: it accepts and fully processes one TCP connection at a
  time. A slow request from one client delays every other client, by design (see Section 6.2 of
  the lab).
- Only the `GET` method is supported. Every other method gets `405 Method Not Allowed`.
- Routing is a small, fixed set of hardcoded paths, not a general router. Adding a new service
  means adding a new `if` branch in `RequestRouter`, on purpose.
- There is no authentication, no encryption (no HTTPS), no request body parsing, and no
  persistence layer. The server does not store any information between requests.
- This is a teaching project, not a production-ready HTTP server. It has no protection against
  slow-client attacks, no configurable timeouts, and no request size limits.

## 13. Author and acknowledgment

**Author:** Paula Lozano ([lozano.paula1021@gmail.com](mailto:lozano.paula1021@gmail.com)).

This lab and its supporting material were assigned by Prof. Luis Daniel Benavides Navarro,
Escuela Colombiana de Ingeniería. Part of the introductory material referenced in the course
content is based on the official Java networking tutorials at
[docs.oracle.com/javase/tutorial/networking](https://docs.oracle.com/javase/tutorial/networking/index.html).
Implementation help was provided by Claude Code (Anthropic).

## 14. Discussion questions

1. **Why does a single HTML page cause several HTTP requests?** The HTML document only describes
   references to other resources (a `<script src>` tag, `<img src>` tags, later `fetch()` calls).
   The browser has to make a separate request for each one once it reads the document, so one page
   view turns into one request for the document plus one for every script, image, and API call it
   needs.
2. **Why must image responses be treated as bytes rather than text?** Images are binary data.
   Decoding or re-encoding them through a text charset (as a `String`) can corrupt byte sequences
   that do not map cleanly to that charset. Reading and writing them as `byte[]` from start to
   finish is the only way to guarantee the bytes the browser gets are identical to the bytes on
   disk.
3. **What is the role of the response content type?** It tells the browser how to read the body:
   build a page from HTML, run a script, decode and draw an image, or parse a JSON object. Serving
   the wrong content type makes the browser mishandle an otherwise correct response, for example
   showing JavaScript source as plain text instead of running it.
4. **What is hardcoded in this design, and what would a routing framework eventually make
   general?** The mapping from a specific path (`/api/greeting`, `/api/square`, `/api/time`,
   `/api/health`) to a specific handler is hardcoded as plain `if` statements in `RequestRouter`.
   A routing framework would turn this into a table or an annotation-driven dispatch mechanism
   (path patterns, parameter binding, middleware chains), so new routes could be added without
   touching a central dispatcher. The cost is that it hides the exact mechanism this lab wants
   visible.
5. **Why can the browser stay responsive while the server still handles requests one at a time?**
   Because responsiveness is a property of the client's own event loop, not of the server.
   `fetch()` is asynchronous on the JavaScript side: it registers a callback and gives control back
   to the browser right away, so the user can keep using the page while the response is still on
   its way, no matter how long the server takes to produce it.
6. **What changed when the server moved to EC2? What stayed the same?** What changed: the network
   location (a public IP or DNS name instead of `localhost`), the operating environment (a remote
   Linux instance instead of a local machine), and the exposure surface (a security group now
   controls who can even reach the port). What stayed the same: the application code, its
   sequential accept loop, its hardcoded routes, and its single-instance capacity limit. Moving
   host never made it concurrent or distributed.
7. **What happens when two users send slow requests at almost the same time?** The second user's
   request sits in the operating system's connection backlog until the server finishes handling
   the first user's request and calls `accept()` again. From the second user's point of view, the
   response is simply delayed by however long the first request took. The sequential server has no
   way to work on both at once.
8. **What is the next architectural limitation you would address, and why should concurrency come
   before load balancing?** The next limitation is the single-threaded accept loop itself: one
   slow or long-running request currently blocks every other client. Concurrency (a thread per
   connection, or a thread pool) has to come first, because load balancing only spreads requests
   across multiple server instances. If each instance is still sequential inside, adding more of
   them just multiplies the same one-request-at-a-time bottleneck instead of fixing it.

## 15. Preliminary exercises from Part 1

Part 2 says it "begins after the minimal HTTP server from section 4.4" of the earlier networking
guide, so this repository also keeps the small standalone exercises that guide asked for, under
[`src/main/java/edu/eci/arsw/networking/part1`](src/main/java/edu/eci/arsw/networking/part1). They
are kept separate from the graded Part 2 application, and are not wired into `NetworkingServer` or
`RequestRouter` in any way.

| Class | Exercise | What it does |
|---|---|---|
| [`UrlComponentsPrinter`](src/main/java/edu/eci/arsw/networking/part1/UrlComponentsPrinter.java) | Section 3.1, Exercise 1 | Builds a `URL` and prints its protocol, authority, host, port, path, query, file, and ref |
| [`UrlPageDownloader`](src/main/java/edu/eci/arsw/networking/part1/UrlPageDownloader.java) | Section 3.2, Exercise 2 | Asks for a URL, reads the page it points to, and saves it as `result.html` |
| [`EchoServer`](src/main/java/edu/eci/arsw/networking/part1/EchoServer.java) | Section 4.2, Figure 4 | Accepts one connection and echoes back every line, with a `Response: ` prefix, until it receives `Bye.` |
| [`EchoClient`](src/main/java/edu/eci/arsw/networking/part1/EchoClient.java) | Section 4.1, Figure 3 | Connects to `EchoServer`, sends each line typed on the keyboard, and prints the echoed response |

Run them one at a time after `mvn compile`:

```bash
java -cp target/classes edu.eci.arsw.networking.part1.UrlComponentsPrinter "http://ldbn.escuelaing.edu.co:80/index.html"
java -cp target/classes edu.eci.arsw.networking.part1.UrlPageDownloader
java -cp target/classes edu.eci.arsw.networking.part1.EchoServer 36000
java -cp target/classes edu.eci.arsw.networking.part1.EchoClient 127.0.0.1 36000
```

`EchoIntegrationTest` exercises the server and client pair over a real socket as part of
`mvn test`. `UrlComponentsPrinter` and `UrlPageDownloader` are interactive console programs and are
checked by hand instead.
