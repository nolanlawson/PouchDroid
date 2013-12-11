package com.nolanlawson.couchdroid.pouch;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.type.TypeReference;

import android.app.Activity;
import android.text.TextUtils;

import com.nolanlawson.couchdroid.CouchDroidRuntime;
import com.nolanlawson.couchdroid.util.JsonUtil;
import com.nolanlawson.couchdroid.util.UtilLogger;

public class PouchDB<T extends PouchDocument> {

    private static UtilLogger log = new UtilLogger(PouchDB.class);

    private static final AtomicInteger POUCH_IDS = new AtomicInteger(0);

    private int id;
    private CouchDroidRuntime runtime;

    private Class<T> documentClass;

    /**
     * <p>
     * This method creates a database or opens an existing one. If you use a
     * <code>http://domain.com/dbname</code> then PouchDB will work as a client
     * to an online CouchDB instance, otherwise it will create a local database
     * using a backend that is present.
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong>
     * </p>
     * 
     * <ol>
     * <li>If you are also using indexedDB directly, PouchDB will use
     * <code>_pouch_</code> to prefix the internal database names, dont manually
     * create databases with the same prefix.</li>
     * <li>When acting as a client on Node any other options given will be
     * passed to <a href="https://github.com/mikeal/request">request</a>.</li>
     * </ol>
     * 
     * <ul>
     * <li><code>options.name</code>: You can omit the name argument and specify
     * it via options.</li>
     * <li><code>options.auto_compaction</code>: This turns on auto compaction
     * (experimental).</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">);</span>
     * <span class="c1">// or</span>
     * <span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;http://localhost:5984/dbname&#39;</span><span class="p">);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#create_database'>http://pouchdb.com/api.html#create_datab
     *      a s e < / a >
     */
    public static <T extends PouchDocument> PouchDB<T> newPouchDB(Class<T> documentClass, CouchDroidRuntime runtime) {
        return newPouchDB(documentClass, runtime, null, false);
    }

    /**
     * <p>
     * This method creates a database or opens an existing one. If you use a
     * <code>http://domain.com/dbname</code> then PouchDB will work as a client
     * to an online CouchDB instance, otherwise it will create a local database
     * using a backend that is present.
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong>
     * </p>
     * 
     * <ol>
     * <li>If you are also using indexedDB directly, PouchDB will use
     * <code>_pouch_</code> to prefix the internal database names, dont manually
     * create databases with the same prefix.</li>
     * <li>When acting as a client on Node any other options given will be
     * passed to <a href="https://github.com/mikeal/request">request</a>.</li>
     * </ol>
     * 
     * <ul>
     * <li><code>options.name</code>: You can omit the name argument and specify
     * it via options.</li>
     * <li><code>options.auto_compaction</code>: This turns on auto compaction
     * (experimental).</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">);</span>
     * <span class="c1">// or</span>
     * <span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;http://localhost:5984/dbname&#39;</span><span class="p">);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#create_database'>http://pouchdb.com/api.html#create_datab
     *      a s e < / a >
     */
    public static <T extends PouchDocument> PouchDB<T> newPouchDB(Class<T> documentClass, CouchDroidRuntime runtime,
            String name) {
        return newPouchDB(documentClass, runtime, name, false);
    }

    /**
     * <p>
     * This method creates a database or opens an existing one. If you use a
     * <code>http://domain.com/dbname</code> then PouchDB will work as a client
     * to an online CouchDB instance, otherwise it will create a local database
     * using a backend that is present.
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong>
     * </p>
     * 
     * <ol>
     * <li>If you are also using indexedDB directly, PouchDB will use
     * <code>_pouch_</code> to prefix the internal database names, dont manually
     * create databases with the same prefix.</li>
     * <li>When acting as a client on Node any other options given will be
     * passed to <a href="https://github.com/mikeal/request">request</a>.</li>
     * </ol>
     * 
     * <ul>
     * <li><code>options.name</code>: You can omit the name argument and specify
     * it via options.</li>
     * <li><code>options.auto_compaction</code>: This turns on auto compaction
     * (experimental).</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">);</span>
     * <span class="c1">// or</span>
     * <span class="kd">var</span> <span class="nx">db</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">PouchDB</span><span class="p">(</span><span class="s1">&#39;http://localhost:5984/dbname&#39;</span><span class="p">);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#create_database'>http://pouchdb.com/api.html#create_datab
     *      a s e < / a >
     */
    public static <T extends PouchDocument> PouchDB<T> newPouchDB(Class<T> documentClass, CouchDroidRuntime runtime,
            String name, boolean autoCompaction) {
        return new PouchDB<T>(documentClass, runtime, name, autoCompaction);
    }

    private PouchDB(Class<T> documentClass, CouchDroidRuntime runtime, String name, boolean autoCompaction) {
        this.id = POUCH_IDS.incrementAndGet();
        this.documentClass = documentClass;
        this.runtime = runtime;

        runtime.loadJavascript(new StringBuilder("CouchDroid.pouchDBs[")
                .append(id).append("] = new PouchDB(")
                .append(JsonUtil.simpleMap("name", name, "adapter", "websql", "autoCompaction", autoCompaction))
                .append(");"));
    }

    /**
     * <p>
     * Delete database with given name
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong> With a remote couch on node options are passed to
     * <a href="https://github.com/mikeal/request">request</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">destroy</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">info</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * // * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_database'>http://pouchdb.com/api.html#delete_databa
     *      s e < / a >
     */
    public void destroy(StandardCallback callback) {
        destroy(null, callback);
    }

    /**
     * <p>
     * Delete database with given name
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong> With a remote couch on node options are passed to
     * <a href="https://github.com/mikeal/request">request</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">destroy</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">info</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * // * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_database'>http://pouchdb.com/api.html#delete_databa
     *      s e < / a >
     */
    public void destroy(Map<String, Object> options) {
        destroy(options, null);
    }

    /**
     * <p>
     * Delete database with given name
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong> With a remote couch on node options are passed to
     * <a href="https://github.com/mikeal/request">request</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">destroy</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">info</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * // * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_database'>http://pouchdb.com/api.html#delete_databa
     *      s e < / a >
     */
    public void destroy() {
        destroy(null, null);
    }

    /**
     * <p>
     * Delete database with given name
     * </p>
     * 
     * <p>
     * <strong>Notes:</strong> With a remote couch on node options are passed to
     * <a href="https://github.com/mikeal/request">request</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">destroy</span><span class="p">(</span><span class="s1">&#39;dbname&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">info</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * // * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_database'>http://pouchdb.com/api.html#delete_databa
     *      s e < / a >
     */
    public void destroy(Map<String, Object> options, StandardCallback callback) {
        loadAction("destroy", options, callback);

        // won't need this database anymore
        runtime.loadJavascript(new StringBuilder("delete CouchDroid.pouchDBs[").append(id).append("];"));
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */
    public void put(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("put", PouchDocumentMapper.toJson(doc), options, callback);
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */
    public void put(T doc, Map<String, Object> options) {
        put(doc, options, null);
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */
    public void put(T doc, StandardCallback callback) {
        put(doc, null, callback);
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */

    public void put(T doc) {
        put(doc, null, null);
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */

    public void post(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("post", PouchDocumentMapper.toJson(doc), options, callback);
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */

    public void post(T doc, Map<String, Object> options) {
        post(doc, options, null);
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */

    public void post(T doc, StandardCallback callback) {
        post(doc, null, callback);
    }

    /**
     * 
     <h2>Create / Update a document<a id="create_document"></a></h2>
     * 
     * <h3>Using db.put()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document or update an existing document. If the document
     * already exists you must specify its revision <code>_rev</code>, otherwise
     * a conflict will occur.
     * </p>
     * 
     * <p>
     * There are some restrictions on valid property names of the documents,
     * these are explained <a
     * href="http://wiki.apache.org/couchdb/HTTP_Document_API#Special_Fields"
     * >here</a>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * 
     * <span class="nx">db</span><span class="p">.</span><span class="nx">put</span><span class="p">({</span>
     *   <span class="nx">_id</span><span class="o">:</span> <span class="s1">&#39;mydoc&#39;</span><span class="p">,</span>
     *   <span class="nx">_rev</span><span class="o">:</span> <span class="s1">&#39;1-A6157A5EA545C99B00FF904EEF05FD9F&#39;</span><span class="p">,</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lets Dance&#39;</span><span class="p">,</span>
     * <span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">})</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h3>Using db.post()</h3>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Create a new document and let PouchDB generate an <code>_id</code> for
     * it.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">post</span><span class="p">({</span>
     *   <span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Heroes&#39;</span>
     * <span class="p">},</span> <span class="kd">function</span> <span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href =
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document
     *      < / a >
     * @param doc
     * @param callback
     */
    public void post(T doc) {
        post(doc, null, null);
    }

    /**
     * <h2>Fetch document<a id="fetch_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="nx">docid</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Retrieves a document, specified by <code>docid</code>.
     * </p>
     * 
     * <ul>
     * <li><code>options.rev</code>: Fetch specific revision of a document.
     * Defaults to winning revision (see <a
     * href="http://guide.couchdb.org/draft/conflicts.html">couchdb guide</a>.</li>
     * <li><code>options.revs</code>: Include revision history of the document</li>
     * <li><code>options.revs_info</code>: Include a list of revisions of the
     * document, and their availability.</li>
     * <li><code>options.open_revs</code>: Fetch all leaf revisions if open
     * <em>revs=&quot;all&quot; or fetch all leaf revisions specified in open</em>
     * revs array. Leaves will be returned in the same order as specified in
     * input array</li>
     * <li><code>options.conflicts</code>: If specified conflicting leaf
     * revisions will be attached in <code>_conflicts</code> array</li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * <li><code>options.local_seq</code>: Include sequence number of the
     * revision in the database</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;title&quot;</span><span class="o">:</span> <span class="s2">&quot;Rock and Roll Heart&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#fetch_document'>http://pouchdb.com/api.html#fetch_document</a
     *      >
     * 
     * @param docid
     * @param options
     * @param callback
     */
    public void get(String docid, Map<String, Object> options, final GetCallback<T> callback) {
        loadAction("get", JsonUtil.simpleString(docid), options, callback == null ? null : new Callback<T>() {

            @Override
            public void onCallback(PouchError err, T info) {
                callback.onCallback(err, info);
            }

            @Override
            public Class<?> getPrimaryClass() {
                return documentClass;
            }

            @Override
            public Class<?> getGenericClass() {
                return null;
            }
        });
    }

    /**
     * <h2>Fetch document<a id="fetch_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="nx">docid</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Retrieves a document, specified by <code>docid</code>.
     * </p>
     * 
     * <ul>
     * <li><code>options.rev</code>: Fetch specific revision of a document.
     * Defaults to winning revision (see <a
     * href="http://guide.couchdb.org/draft/conflicts.html">couchdb guide</a>.</li>
     * <li><code>options.revs</code>: Include revision history of the document</li>
     * <li><code>options.revs_info</code>: Include a list of revisions of the
     * document, and their availability.</li>
     * <li><code>options.open_revs</code>: Fetch all leaf revisions if open
     * <em>revs=&quot;all&quot; or fetch all leaf revisions specified in open</em>
     * revs array. Leaves will be returned in the same order as specified in
     * input array</li>
     * <li><code>options.conflicts</code>: If specified conflicting leaf
     * revisions will be attached in <code>_conflicts</code> array</li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * <li><code>options.local_seq</code>: Include sequence number of the
     * revision in the database</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;title&quot;</span><span class="o">:</span> <span class="s2">&quot;Rock and Roll Heart&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#fetch_document'>http://pouchdb.com/api.html#fetch_document</a
     *      >
     * 
     * @param docid
     * @param options
     * @param callback
     */
    public void get(String docid, Map<String, Object> options) {
        get(docid, options, null);
    }

    /**
     * <h2>Fetch document<a id="fetch_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="nx">docid</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Retrieves a document, specified by <code>docid</code>.
     * </p>
     * 
     * <ul>
     * <li><code>options.rev</code>: Fetch specific revision of a document.
     * Defaults to winning revision (see <a
     * href="http://guide.couchdb.org/draft/conflicts.html">couchdb guide</a>.</li>
     * <li><code>options.revs</code>: Include revision history of the document</li>
     * <li><code>options.revs_info</code>: Include a list of revisions of the
     * document, and their availability.</li>
     * <li><code>options.open_revs</code>: Fetch all leaf revisions if open
     * <em>revs=&quot;all&quot; or fetch all leaf revisions specified in open</em>
     * revs array. Leaves will be returned in the same order as specified in
     * input array</li>
     * <li><code>options.conflicts</code>: If specified conflicting leaf
     * revisions will be attached in <code>_conflicts</code> array</li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * <li><code>options.local_seq</code>: Include sequence number of the
     * revision in the database</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;title&quot;</span><span class="o">:</span> <span class="s2">&quot;Rock and Roll Heart&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#fetch_document'>http://pouchdb.com/api.html#fetch_document</a
     *      >
     * 
     * @param docid
     * @param options
     * @param callback
     */
    public void get(String docid, GetCallback<T> callback) {
        get(docid, null, callback);
    }

    /**
     * <h2>Fetch document<a id="fetch_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="nx">docid</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Retrieves a document, specified by <code>docid</code>.
     * </p>
     * 
     * <ul>
     * <li><code>options.rev</code>: Fetch specific revision of a document.
     * Defaults to winning revision (see <a
     * href="http://guide.couchdb.org/draft/conflicts.html">couchdb guide</a>.</li>
     * <li><code>options.revs</code>: Include revision history of the document</li>
     * <li><code>options.revs_info</code>: Include a list of revisions of the
     * document, and their availability.</li>
     * <li><code>options.open_revs</code>: Fetch all leaf revisions if open
     * <em>revs=&quot;all&quot; or fetch all leaf revisions specified in open</em>
     * revs array. Leaves will be returned in the same order as specified in
     * input array</li>
     * <li><code>options.conflicts</code>: If specified conflicting leaf
     * revisions will be attached in <code>_conflicts</code> array</li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * <li><code>options.local_seq</code>: Include sequence number of the
     * revision in the database</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;title&quot;</span><span class="o">:</span> <span class="s2">&quot;Rock and Roll Heart&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A6157A5EA545C99B00FF904EEF05FD9F&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#fetch_document'>http://pouchdb.com/api.html#fetch_document</a
     *      >
     * 
     * @param docid
     * @param options
     * @param callback
     */
    public void get(String docid) {
        get(docid, null, null);
    }

    /**
     * 
     * <h2>Delete document<a id="delete_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Delete a document, <code>doc</code> is required to be a document with at
     * least an <code>_id</code> and a <code>_rev</code> property, sending the
     * full document will work.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span>
     *   <span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;2-9AF304BE281790604D1D8A4B0F4C9ADB&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_document'>http://pouchdb.com/api.html#delete_document</a
     *      >
     * @param doc
     * @param options
     * @param callback
     */
    public void remove(T doc, Map<String, Object> options, StandardCallback callback) {
        loadAction("remove", PouchDocumentMapper.toJson(doc), options, callback);
    }

    /**
     * 
     * <h2>Delete document<a id="delete_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Delete a document, <code>doc</code> is required to be a document with at
     * least an <code>_id</code> and a <code>_rev</code> property, sending the
     * full document will work.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span>
     *   <span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;2-9AF304BE281790604D1D8A4B0F4C9ADB&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_document'>http://pouchdb.com/api.html#delete_document</a
     *      >
     * @param doc
     * @param options
     * @param callback
     */
    public void remove(T doc, Map<String, Object> options) {
        remove(doc, options, null);
    }

    /**
     * 
     * <h2>Delete document<a id="delete_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Delete a document, <code>doc</code> is required to be a document with at
     * least an <code>_id</code> and a <code>_rev</code> property, sending the
     * full document will work.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span>
     *   <span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;2-9AF304BE281790604D1D8A4B0F4C9ADB&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_document'>http://pouchdb.com/api.html#delete_document</a
     *      >
     * @param doc
     * @param options
     * @param callback
     */
    public void remove(T doc, StandardCallback callback) {
        remove(doc, null, callback);
    }

    /**
     * 
     * <h2>Delete document<a id="delete_document"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Delete a document, <code>doc</code> is required to be a document with at
     * least an <code>_id</code> and a <code>_rev</code> property, sending the
     * full document will work.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">get</span><span class="p">(</span><span class="s1">&#39;mydoc&#39;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">doc</span><span class="p">)</span> <span class="p">{</span>
     *   <span class="nx">db</span><span class="p">.</span><span class="nx">remove</span><span class="p">(</span><span class="nx">doc</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;mydoc&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;2-9AF304BE281790604D1D8A4B0F4C9ADB&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#delete_document'>http://pouchdb.com/api.html#delete_document</a
     *      >
     * @param doc
     * @param options
     * @param callback
     */
    public void remove(T doc) {
        remove(doc, null, null);
    }

    /**
     * <h2>Create a batch of documents<a id="batch_create"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">(</span><span class="nx">docs</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Modify, create or delete multiple documents. The docs argument is an
     * object with property <code>docs</code> which is an array of documents.
     * You can also specify a <code>new_edits</code> property on the
     * <code>docs</code> object that when set to <code>false</code> allows you
     * to post <a href=
     * "http://wiki.apache.org/couchdb/HTTP_Bulk_Document_API#Posting_Existing_Revisions"
     * >existing documents</a>.
     * </p>
     * 
     * <p>
     * If you omit an <code>_id</code> parameter on a given document, the
     * database will create a new document and assign an ID for you. To update a
     * document you must include both an <code>_id</code> parameter and a
     * <code>_rev</code> parameter, which should match the ID and revision of
     * the document on which to base your updates. Finally, to delete a
     * document, include a <code>_deleted</code> parameter with the value
     * <code>true</code>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">({</span><span class="nx">docs</span><span class="o">:</span> <span class="p">[{</span><span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lisa Says&#39;</span><span class="p">}]},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">[{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;828124B9-3973-4AF3-9DFD-A94CE4544005&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A8BC08745E62E58830CA066D99E5F457&quot;</span>
     * <span class="p">}]</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_create'>http://pouchdb.com/api.html#batch_create</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void bulkDocs(List<T> docs, Map<String, Object> options, BulkCallback callback) {

        String mapAsJson = new StringBuilder("{\"docs\":").append(PouchDocumentMapper.toJson(docs)).append("}")
                .toString();

        loadAction("bulkDocs", mapAsJson, options, callback);
    }

    /**
     * <h2>Create a batch of documents<a id="batch_create"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">(</span><span class="nx">docs</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Modify, create or delete multiple documents. The docs argument is an
     * object with property <code>docs</code> which is an array of documents.
     * You can also specify a <code>new_edits</code> property on the
     * <code>docs</code> object that when set to <code>false</code> allows you
     * to post <a href=
     * "http://wiki.apache.org/couchdb/HTTP_Bulk_Document_API#Posting_Existing_Revisions"
     * >existing documents</a>.
     * </p>
     * 
     * <p>
     * If you omit an <code>_id</code> parameter on a given document, the
     * database will create a new document and assign an ID for you. To update a
     * document you must include both an <code>_id</code> parameter and a
     * <code>_rev</code> parameter, which should match the ID and revision of
     * the document on which to base your updates. Finally, to delete a
     * document, include a <code>_deleted</code> parameter with the value
     * <code>true</code>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">({</span><span class="nx">docs</span><span class="o">:</span> <span class="p">[{</span><span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lisa Says&#39;</span><span class="p">}]},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">[{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;828124B9-3973-4AF3-9DFD-A94CE4544005&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A8BC08745E62E58830CA066D99E5F457&quot;</span>
     * <span class="p">}]</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_create'>http://pouchdb.com/api.html#batch_create</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void bulkDocs(List<T> docs, Map<String, Object> options) {
        bulkDocs(docs, options, null);
    }

    /**
     * <h2>Create a batch of documents<a id="batch_create"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">(</span><span class="nx">docs</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Modify, create or delete multiple documents. The docs argument is an
     * object with property <code>docs</code> which is an array of documents.
     * You can also specify a <code>new_edits</code> property on the
     * <code>docs</code> object that when set to <code>false</code> allows you
     * to post <a href=
     * "http://wiki.apache.org/couchdb/HTTP_Bulk_Document_API#Posting_Existing_Revisions"
     * >existing documents</a>.
     * </p>
     * 
     * <p>
     * If you omit an <code>_id</code> parameter on a given document, the
     * database will create a new document and assign an ID for you. To update a
     * document you must include both an <code>_id</code> parameter and a
     * <code>_rev</code> parameter, which should match the ID and revision of
     * the document on which to base your updates. Finally, to delete a
     * document, include a <code>_deleted</code> parameter with the value
     * <code>true</code>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">({</span><span class="nx">docs</span><span class="o">:</span> <span class="p">[{</span><span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lisa Says&#39;</span><span class="p">}]},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">[{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;828124B9-3973-4AF3-9DFD-A94CE4544005&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A8BC08745E62E58830CA066D99E5F457&quot;</span>
     * <span class="p">}]</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_create'>http://pouchdb.com/api.html#batch_create</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void bulkDocs(List<T> docs, BulkCallback callback) {
        bulkDocs(docs, null, callback);
    }

    /**
     * <h2>Create a batch of documents<a id="batch_create"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">(</span><span class="nx">docs</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Modify, create or delete multiple documents. The docs argument is an
     * object with property <code>docs</code> which is an array of documents.
     * You can also specify a <code>new_edits</code> property on the
     * <code>docs</code> object that when set to <code>false</code> allows you
     * to post <a href=
     * "http://wiki.apache.org/couchdb/HTTP_Bulk_Document_API#Posting_Existing_Revisions"
     * >existing documents</a>.
     * </p>
     * 
     * <p>
     * If you omit an <code>_id</code> parameter on a given document, the
     * database will create a new document and assign an ID for you. To update a
     * document you must include both an <code>_id</code> parameter and a
     * <code>_rev</code> parameter, which should match the ID and revision of
     * the document on which to base your updates. Finally, to delete a
     * document, include a <code>_deleted</code> parameter with the value
     * <code>true</code>.
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">bulkDocs</span><span class="p">({</span><span class="nx">docs</span><span class="o">:</span> <span class="p">[{</span><span class="nx">title</span><span class="o">:</span> <span class="s1">&#39;Lisa Says&#39;</span><span class="p">}]},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">[{</span>
     *   <span class="s2">&quot;ok&quot;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;828124B9-3973-4AF3-9DFD-A94CE4544005&quot;</span><span class="p">,</span>
     *   <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-A8BC08745E62E58830CA066D99E5F457&quot;</span>
     * <span class="p">}]</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_create'>http://pouchdb.com/api.html#batch_create</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void bulkDocs(List<T> docs) {
        bulkDocs(docs, null, null);
    }

    /**
     * 
     * <h2>Fetch documents<a id="batch_fetch"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">([</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Fetch multiple documents, deleted document are only included if
     * <code>options.keys</code> is specified.
     * </p>
     * 
     * <ul>
     * <li><code>options.include_docs</code>: Include the document in each row
     * in the <code>doc</code> field
     * 
     * <ul>
     * <li><code>options.conflicts</code>: Include conflicts in the
     * <code>_conflicts</code> field of a doc</li>
     * </ul>
     * </li>
     * <li><code>options.startkey</code> &amp; <code>options.endkey</code>: Get
     * documents with keys in a certain range</li>
     * <li><code>options.descending</code>: Reverse the order of the output
     * table</li>
     * <li><code>options.keys</code>: array of keys you want to get
     * 
     * <ul>
     * <li>neither <code>startkey</code> nor <code>endkey</code> can be
     * specified with this option</li>
     * <li>the rows are returned in the same order as the supplied
     * &quot;keys&quot; array</li>
     * <li>the row for a deleted document will have the revision ID of the
     * deletion, and an extra key &quot;deleted&quot;:true in the
     * &quot;value&quot; property</li>
     * <li>the row for a nonexistent document will just contain an
     * &quot;error&quot; property with the value &quot;not_found&quot;</li>
     * </ul>
     * </li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">({</span><span class="nx">include_docs</span><span class="o">:</span> <span class="kc">true</span><span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;total_rows&quot;</span><span class="o">:</span> <span class="mi">1</span><span class="p">,</span>
     *   <span class="s2">&quot;rows&quot;</span><span class="o">:</span> <span class="p">[{</span>
     *     <span class="s2">&quot;doc&quot;</span><span class="o">:</span> <span class="p">{</span>
     *       <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;blog_post&quot;</span><span class="o">:</span> <span class="s2">&quot;my blog post&quot;</span>
     *     <span class="p">},</span>
     *    <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;key&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;value&quot;</span><span class="o">:</span> <span class="p">{</span>
     *     <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span>
     *    <span class="p">}</span>
     *  <span class="p">}]</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_fetch'>http://pouchdb.com/api.html#batch_fetch</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void allDocs(Map<String, Object> options, final AllDocsCallback<T> callback) {
        loadAction("allDocs", options, callback == null ? null : new AllDocsCallback<T>() {

            @Override
            public void onCallback(PouchError err, AllDocsInfo<T> info) {
                callback.onCallback(err, info);
            }

            @Override
            public Class<?> getGenericClass() {
                return documentClass;
            }
        });
    }

    /**
     * 
     * <h2>Fetch documents<a id="batch_fetch"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">([</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Fetch multiple documents, deleted document are only included if
     * <code>options.keys</code> is specified.
     * </p>
     * 
     * <ul>
     * <li><code>options.include_docs</code>: Include the document in each row
     * in the <code>doc</code> field
     * 
     * <ul>
     * <li><code>options.conflicts</code>: Include conflicts in the
     * <code>_conflicts</code> field of a doc</li>
     * </ul>
     * </li>
     * <li><code>options.startkey</code> &amp; <code>options.endkey</code>: Get
     * documents with keys in a certain range</li>
     * <li><code>options.descending</code>: Reverse the order of the output
     * table</li>
     * <li><code>options.keys</code>: array of keys you want to get
     * 
     * <ul>
     * <li>neither <code>startkey</code> nor <code>endkey</code> can be
     * specified with this option</li>
     * <li>the rows are returned in the same order as the supplied
     * &quot;keys&quot; array</li>
     * <li>the row for a deleted document will have the revision ID of the
     * deletion, and an extra key &quot;deleted&quot;:true in the
     * &quot;value&quot; property</li>
     * <li>the row for a nonexistent document will just contain an
     * &quot;error&quot; property with the value &quot;not_found&quot;</li>
     * </ul>
     * </li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">({</span><span class="nx">include_docs</span><span class="o">:</span> <span class="kc">true</span><span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;total_rows&quot;</span><span class="o">:</span> <span class="mi">1</span><span class="p">,</span>
     *   <span class="s2">&quot;rows&quot;</span><span class="o">:</span> <span class="p">[{</span>
     *     <span class="s2">&quot;doc&quot;</span><span class="o">:</span> <span class="p">{</span>
     *       <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;blog_post&quot;</span><span class="o">:</span> <span class="s2">&quot;my blog post&quot;</span>
     *     <span class="p">},</span>
     *    <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;key&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;value&quot;</span><span class="o">:</span> <span class="p">{</span>
     *     <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span>
     *    <span class="p">}</span>
     *  <span class="p">}]</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_fetch'>http://pouchdb.com/api.html#batch_fetch</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void allDocs(AllDocsCallback<T> callback) {
        allDocs(null, callback);
    }

    /**
     * 
     * <h2>Fetch documents<a id="batch_fetch"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">([</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Fetch multiple documents, deleted document are only included if
     * <code>options.keys</code> is specified.
     * </p>
     * 
     * <ul>
     * <li><code>options.include_docs</code>: Include the document in each row
     * in the <code>doc</code> field
     * 
     * <ul>
     * <li><code>options.conflicts</code>: Include conflicts in the
     * <code>_conflicts</code> field of a doc</li>
     * </ul>
     * </li>
     * <li><code>options.startkey</code> &amp; <code>options.endkey</code>: Get
     * documents with keys in a certain range</li>
     * <li><code>options.descending</code>: Reverse the order of the output
     * table</li>
     * <li><code>options.keys</code>: array of keys you want to get
     * 
     * <ul>
     * <li>neither <code>startkey</code> nor <code>endkey</code> can be
     * specified with this option</li>
     * <li>the rows are returned in the same order as the supplied
     * &quot;keys&quot; array</li>
     * <li>the row for a deleted document will have the revision ID of the
     * deletion, and an extra key &quot;deleted&quot;:true in the
     * &quot;value&quot; property</li>
     * <li>the row for a nonexistent document will just contain an
     * &quot;error&quot; property with the value &quot;not_found&quot;</li>
     * </ul>
     * </li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">({</span><span class="nx">include_docs</span><span class="o">:</span> <span class="kc">true</span><span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;total_rows&quot;</span><span class="o">:</span> <span class="mi">1</span><span class="p">,</span>
     *   <span class="s2">&quot;rows&quot;</span><span class="o">:</span> <span class="p">[{</span>
     *     <span class="s2">&quot;doc&quot;</span><span class="o">:</span> <span class="p">{</span>
     *       <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;blog_post&quot;</span><span class="o">:</span> <span class="s2">&quot;my blog post&quot;</span>
     *     <span class="p">},</span>
     *    <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;key&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;value&quot;</span><span class="o">:</span> <span class="p">{</span>
     *     <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span>
     *    <span class="p">}</span>
     *  <span class="p">}]</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_fetch'>http://pouchdb.com/api.html#batch_fetch</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void allDocs(boolean includeDocs, Map<String, Object> otherOptions, final AllDocsCallback<T> callback) {
        // included as a convenience method, because I'm sure otherwise people
        // will forge to set include_docs=true
        Map<String, Object> options = otherOptions != null ? otherOptions : new LinkedHashMap<String, Object>();
        options.put("include_docs", includeDocs);
        allDocs(options, callback);
    }

    /**
     * 
     * <h2>Fetch documents<a id="batch_fetch"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">([</span><span class="nx">options</span><span class="p">],</span> <span class="p">[</span><span class="nx">callback</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Fetch multiple documents, deleted document are only included if
     * <code>options.keys</code> is specified.
     * </p>
     * 
     * <ul>
     * <li><code>options.include_docs</code>: Include the document in each row
     * in the <code>doc</code> field
     * 
     * <ul>
     * <li><code>options.conflicts</code>: Include conflicts in the
     * <code>_conflicts</code> field of a doc</li>
     * </ul>
     * </li>
     * <li><code>options.startkey</code> &amp; <code>options.endkey</code>: Get
     * documents with keys in a certain range</li>
     * <li><code>options.descending</code>: Reverse the order of the output
     * table</li>
     * <li><code>options.keys</code>: array of keys you want to get
     * 
     * <ul>
     * <li>neither <code>startkey</code> nor <code>endkey</code> can be
     * specified with this option</li>
     * <li>the rows are returned in the same order as the supplied
     * &quot;keys&quot; array</li>
     * <li>the row for a deleted document will have the revision ID of the
     * deletion, and an extra key &quot;deleted&quot;:true in the
     * &quot;value&quot; property</li>
     * <li>the row for a nonexistent document will just contain an
     * &quot;error&quot; property with the value &quot;not_found&quot;</li>
     * </ul>
     * </li>
     * <li><code>options.attachments</code>: Include attachment data</li>
     * </ul>
     * 
     * <p>
     * <span></span>
     * </p>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">allDocs</span><span class="p">({</span><span class="nx">include_docs</span><span class="o">:</span> <span class="kc">true</span><span class="p">},</span> <span class="kd">function</span><span class="p">(</span><span class="nx">err</span><span class="p">,</span> <span class="nx">response</span><span class="p">)</span> <span class="p">{</span> <span class="p">});</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s2">&quot;total_rows&quot;</span><span class="o">:</span> <span class="mi">1</span><span class="p">,</span>
     *   <span class="s2">&quot;rows&quot;</span><span class="o">:</span> <span class="p">[{</span>
     *     <span class="s2">&quot;doc&quot;</span><span class="o">:</span> <span class="p">{</span>
     *       <span class="s2">&quot;_id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;_rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span><span class="p">,</span>
     *       <span class="s2">&quot;blog_post&quot;</span><span class="o">:</span> <span class="s2">&quot;my blog post&quot;</span>
     *     <span class="p">},</span>
     *    <span class="s2">&quot;id&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;key&quot;</span><span class="o">:</span> <span class="s2">&quot;0B3358C1-BA4B-4186-8795-9024203EB7DD&quot;</span><span class="p">,</span>
     *    <span class="s2">&quot;value&quot;</span><span class="o">:</span> <span class="p">{</span>
     *     <span class="s2">&quot;rev&quot;</span><span class="o">:</span> <span class="s2">&quot;1-5782E71F1E4BF698FA3793D9D5A96393&quot;</span>
     *    <span class="p">}</span>
     *  <span class="p">}]</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#batch_fetch'>http://pouchdb.com/api.html#batch_fetch</a
     *      >
     * @param docs
     * @param options
     * @param callback
     */
    public void allDocs(boolean includeDocs, AllDocsCallback<T> callback) {
        allDocs(includeDocs, null, callback);
    }

    /**
     * <h2>Replicate a database<a id="replication"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="nx">source</span><span class="p">,</span> <span class="nx">target</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Replicate data from <code>source</code> to <code>target</code>, both the
     * <code>source</code> and <code>target</code> can be strings used to
     * represent a database of a PouchDB object. If
     * <code>options.continuous</code> is <code>true</code> then this will track
     * future changes and also replicate them.
     * </p>
     * 
     * <p>
     * If you want to sync data in both directions you can call this twice
     * reversing the <code>source</code> and <code>target</code> arguments.
     * </p>
     * 
     * <ul>
     * <li><code>options.filter</code>: Reference a filter function from a
     * design document to selectively get updates.</li>
     * <li><code>options.query_params</code>: Query params send to the filter
     * function.</li>
     * <li><code>options.doc_ids</code>: Only replicate docs with these ids.</li>
     * <li><code>options.complete</code>: Function called when all changes have
     * been processed.</li>
     * <li><code>options.onChange</code>: Function called on each change
     * processed..</li>
     * <li><code>options.continuous</code>: If true starts subscribing to future
     * changes in the <code>source</code> database and continue replicating
     * them.</li>
     * <li><code>options.server</code>: Initialize the replication on the
     * server. The response is the CouchDB <code>POST _replicate</code> response
     * and is different from the PouchDB replication response. Also,
     * <code>options.onChange</code> is not supported on server replications.</li>
     * <li><code>options.create_target</code>: Create target database if it does
     * not exist. Only for server replications.</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="s1">&#39;mydb&#39;</span><span class="p">,</span> <span class="s1">&#39;http://localhost:5984/mydb&#39;</span><span class="p">,</span> <span class="p">{</span>
     *   <span class="nx">onChange</span><span class="o">:</span> <span class="nx">onChange</span><span class="p">,</span>
     *   <span class="nx">complete</span><span class="o">:</span> <span class="nx">onComplete</span>
     * <span class="p">});;</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * There are also shorthands for replication given existing PouchDB objects,
     * these behave the same as <code>PouchDB.replicate()</code>:
     * </p>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">to</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * <span class="c1">// or</span>
     * <span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">from</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s1">&#39;ok&#39;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_read&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_written&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;start_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span><span class="p">,</span>
     *   <span class="s1">&#39;end_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Note that the response for server replications (via
     * <code>options.server</code>) is slightly different. See the <a
     * href="http://wiki.apache.org/couchdb/Replication">CouchDB Wiki</a>.
     * </p>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#replication'>http://pouchdb.com/api.html#replication</a
     *      >
     * @param remoteDB
     * @param options
     * @param complete
     */
    public void replicateTo(String remoteDB, Map<String, Object> options, ReplicateCallback complete) {
        loadAction("replicate.to", JsonUtil.simpleString(remoteDB), options, complete, "complete");
    }

    /**
     * <h2>Replicate a database<a id="replication"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="nx">source</span><span class="p">,</span> <span class="nx">target</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Replicate data from <code>source</code> to <code>target</code>, both the
     * <code>source</code> and <code>target</code> can be strings used to
     * represent a database of a PouchDB object. If
     * <code>options.continuous</code> is <code>true</code> then this will track
     * future changes and also replicate them.
     * </p>
     * 
     * <p>
     * If you want to sync data in both directions you can call this twice
     * reversing the <code>source</code> and <code>target</code> arguments.
     * </p>
     * 
     * <ul>
     * <li><code>options.filter</code>: Reference a filter function from a
     * design document to selectively get updates.</li>
     * <li><code>options.query_params</code>: Query params send to the filter
     * function.</li>
     * <li><code>options.doc_ids</code>: Only replicate docs with these ids.</li>
     * <li><code>options.complete</code>: Function called when all changes have
     * been processed.</li>
     * <li><code>options.onChange</code>: Function called on each change
     * processed..</li>
     * <li><code>options.continuous</code>: If true starts subscribing to future
     * changes in the <code>source</code> database and continue replicating
     * them.</li>
     * <li><code>options.server</code>: Initialize the replication on the
     * server. The response is the CouchDB <code>POST _replicate</code> response
     * and is different from the PouchDB replication response. Also,
     * <code>options.onChange</code> is not supported on server replications.</li>
     * <li><code>options.create_target</code>: Create target database if it does
     * not exist. Only for server replications.</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="s1">&#39;mydb&#39;</span><span class="p">,</span> <span class="s1">&#39;http://localhost:5984/mydb&#39;</span><span class="p">,</span> <span class="p">{</span>
     *   <span class="nx">onChange</span><span class="o">:</span> <span class="nx">onChange</span><span class="p">,</span>
     *   <span class="nx">complete</span><span class="o">:</span> <span class="nx">onComplete</span>
     * <span class="p">});;</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * There are also shorthands for replication given existing PouchDB objects,
     * these behave the same as <code>PouchDB.replicate()</code>:
     * </p>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">to</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * <span class="c1">// or</span>
     * <span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">from</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s1">&#39;ok&#39;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_read&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_written&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;start_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span><span class="p">,</span>
     *   <span class="s1">&#39;end_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Note that the response for server replications (via
     * <code>options.server</code>) is slightly different. See the <a
     * href="http://wiki.apache.org/couchdb/Replication">CouchDB Wiki</a>.
     * </p>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#replication'>http://pouchdb.com/api.html#replication</a
     *      >
     * @param remoteDB
     * @param options
     * @param complete
     */
    public void replicateTo(String remoteDB, ReplicateCallback complete) {
        replicateTo(remoteDB, null, complete);
    }
    /**
     * <h2>Replicate a database<a id="replication"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="nx">source</span><span class="p">,</span> <span class="nx">target</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Replicate data from <code>source</code> to <code>target</code>, both the
     * <code>source</code> and <code>target</code> can be strings used to
     * represent a database of a PouchDB object. If
     * <code>options.continuous</code> is <code>true</code> then this will track
     * future changes and also replicate them.
     * </p>
     * 
     * <p>
     * If you want to sync data in both directions you can call this twice
     * reversing the <code>source</code> and <code>target</code> arguments.
     * </p>
     * 
     * <ul>
     * <li><code>options.filter</code>: Reference a filter function from a
     * design document to selectively get updates.</li>
     * <li><code>options.query_params</code>: Query params send to the filter
     * function.</li>
     * <li><code>options.doc_ids</code>: Only replicate docs with these ids.</li>
     * <li><code>options.complete</code>: Function called when all changes have
     * been processed.</li>
     * <li><code>options.onChange</code>: Function called on each change
     * processed..</li>
     * <li><code>options.continuous</code>: If true starts subscribing to future
     * changes in the <code>source</code> database and continue replicating
     * them.</li>
     * <li><code>options.server</code>: Initialize the replication on the
     * server. The response is the CouchDB <code>POST _replicate</code> response
     * and is different from the PouchDB replication response. Also,
     * <code>options.onChange</code> is not supported on server replications.</li>
     * <li><code>options.create_target</code>: Create target database if it does
     * not exist. Only for server replications.</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="s1">&#39;mydb&#39;</span><span class="p">,</span> <span class="s1">&#39;http://localhost:5984/mydb&#39;</span><span class="p">,</span> <span class="p">{</span>
     *   <span class="nx">onChange</span><span class="o">:</span> <span class="nx">onChange</span><span class="p">,</span>
     *   <span class="nx">complete</span><span class="o">:</span> <span class="nx">onComplete</span>
     * <span class="p">});;</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * There are also shorthands for replication given existing PouchDB objects,
     * these behave the same as <code>PouchDB.replicate()</code>:
     * </p>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">to</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * <span class="c1">// or</span>
     * <span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">from</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s1">&#39;ok&#39;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_read&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_written&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;start_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span><span class="p">,</span>
     *   <span class="s1">&#39;end_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Note that the response for server replications (via
     * <code>options.server</code>) is slightly different. See the <a
     * href="http://wiki.apache.org/couchdb/Replication">CouchDB Wiki</a>.
     * </p>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#replication'>http://pouchdb.com/api.html#replication</a
     *      >
     * @param remoteDB
     * @param options
     * @param complete
     */   
    public void replicateTo(String remoteDB) {
        replicateTo(remoteDB, null);
    }
    
    /**
     * <h2>Replicate a database<a id="replication"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="nx">source</span><span class="p">,</span> <span class="nx">target</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Replicate data from <code>source</code> to <code>target</code>, both the
     * <code>source</code> and <code>target</code> can be strings used to
     * represent a database of a PouchDB object. If
     * <code>options.continuous</code> is <code>true</code> then this will track
     * future changes and also replicate them.
     * </p>
     * 
     * <p>
     * If you want to sync data in both directions you can call this twice
     * reversing the <code>source</code> and <code>target</code> arguments.
     * </p>
     * 
     * <ul>
     * <li><code>options.filter</code>: Reference a filter function from a
     * design document to selectively get updates.</li>
     * <li><code>options.query_params</code>: Query params send to the filter
     * function.</li>
     * <li><code>options.doc_ids</code>: Only replicate docs with these ids.</li>
     * <li><code>options.complete</code>: Function called when all changes have
     * been processed.</li>
     * <li><code>options.onChange</code>: Function called on each change
     * processed..</li>
     * <li><code>options.continuous</code>: If true starts subscribing to future
     * changes in the <code>source</code> database and continue replicating
     * them.</li>
     * <li><code>options.server</code>: Initialize the replication on the
     * server. The response is the CouchDB <code>POST _replicate</code> response
     * and is different from the PouchDB replication response. Also,
     * <code>options.onChange</code> is not supported on server replications.</li>
     * <li><code>options.create_target</code>: Create target database if it does
     * not exist. Only for server replications.</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="s1">&#39;mydb&#39;</span><span class="p">,</span> <span class="s1">&#39;http://localhost:5984/mydb&#39;</span><span class="p">,</span> <span class="p">{</span>
     *   <span class="nx">onChange</span><span class="o">:</span> <span class="nx">onChange</span><span class="p">,</span>
     *   <span class="nx">complete</span><span class="o">:</span> <span class="nx">onComplete</span>
     * <span class="p">});;</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * There are also shorthands for replication given existing PouchDB objects,
     * these behave the same as <code>PouchDB.replicate()</code>:
     * </p>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">to</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * <span class="c1">// or</span>
     * <span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">from</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s1">&#39;ok&#39;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_read&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_written&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;start_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span><span class="p">,</span>
     *   <span class="s1">&#39;end_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Note that the response for server replications (via
     * <code>options.server</code>) is slightly different. See the <a
     * href="http://wiki.apache.org/couchdb/Replication">CouchDB Wiki</a>.
     * </p>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#replication'>http://pouchdb.com/api.html#replication</a
     *      >
     * @param remoteDB
     * @param options
     * @param complete
     */
    public void replicateFrom(String remoteDB, Map<String, Object> options, ReplicateCallback complete) {
        loadAction("replicate.from", JsonUtil.simpleString(remoteDB), options, complete, "complete");
    }

    /**
     * <h2>Replicate a database<a id="replication"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="nx">source</span><span class="p">,</span> <span class="nx">target</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Replicate data from <code>source</code> to <code>target</code>, both the
     * <code>source</code> and <code>target</code> can be strings used to
     * represent a database of a PouchDB object. If
     * <code>options.continuous</code> is <code>true</code> then this will track
     * future changes and also replicate them.
     * </p>
     * 
     * <p>
     * If you want to sync data in both directions you can call this twice
     * reversing the <code>source</code> and <code>target</code> arguments.
     * </p>
     * 
     * <ul>
     * <li><code>options.filter</code>: Reference a filter function from a
     * design document to selectively get updates.</li>
     * <li><code>options.query_params</code>: Query params send to the filter
     * function.</li>
     * <li><code>options.doc_ids</code>: Only replicate docs with these ids.</li>
     * <li><code>options.complete</code>: Function called when all changes have
     * been processed.</li>
     * <li><code>options.onChange</code>: Function called on each change
     * processed..</li>
     * <li><code>options.continuous</code>: If true starts subscribing to future
     * changes in the <code>source</code> database and continue replicating
     * them.</li>
     * <li><code>options.server</code>: Initialize the replication on the
     * server. The response is the CouchDB <code>POST _replicate</code> response
     * and is different from the PouchDB replication response. Also,
     * <code>options.onChange</code> is not supported on server replications.</li>
     * <li><code>options.create_target</code>: Create target database if it does
     * not exist. Only for server replications.</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="s1">&#39;mydb&#39;</span><span class="p">,</span> <span class="s1">&#39;http://localhost:5984/mydb&#39;</span><span class="p">,</span> <span class="p">{</span>
     *   <span class="nx">onChange</span><span class="o">:</span> <span class="nx">onChange</span><span class="p">,</span>
     *   <span class="nx">complete</span><span class="o">:</span> <span class="nx">onComplete</span>
     * <span class="p">});;</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * There are also shorthands for replication given existing PouchDB objects,
     * these behave the same as <code>PouchDB.replicate()</code>:
     * </p>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">to</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * <span class="c1">// or</span>
     * <span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">from</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s1">&#39;ok&#39;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_read&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_written&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;start_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span><span class="p">,</span>
     *   <span class="s1">&#39;end_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Note that the response for server replications (via
     * <code>options.server</code>) is slightly different. See the <a
     * href="http://wiki.apache.org/couchdb/Replication">CouchDB Wiki</a>.
     * </p>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#replication'>http://pouchdb.com/api.html#replication</a
     *      >
     * @param remoteDB
     * @param options
     * @param complete
     */
    public void replicateFrom(String remoteDB, ReplicateCallback complete) {
        replicateFrom(remoteDB, null, complete);
    }
    /**
     * <h2>Replicate a database<a id="replication"></a></h2>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="nx">source</span><span class="p">,</span> <span class="nx">target</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">])</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Replicate data from <code>source</code> to <code>target</code>, both the
     * <code>source</code> and <code>target</code> can be strings used to
     * represent a database of a PouchDB object. If
     * <code>options.continuous</code> is <code>true</code> then this will track
     * future changes and also replicate them.
     * </p>
     * 
     * <p>
     * If you want to sync data in both directions you can call this twice
     * reversing the <code>source</code> and <code>target</code> arguments.
     * </p>
     * 
     * <ul>
     * <li><code>options.filter</code>: Reference a filter function from a
     * design document to selectively get updates.</li>
     * <li><code>options.query_params</code>: Query params send to the filter
     * function.</li>
     * <li><code>options.doc_ids</code>: Only replicate docs with these ids.</li>
     * <li><code>options.complete</code>: Function called when all changes have
     * been processed.</li>
     * <li><code>options.onChange</code>: Function called on each change
     * processed..</li>
     * <li><code>options.continuous</code>: If true starts subscribing to future
     * changes in the <code>source</code> database and continue replicating
     * them.</li>
     * <li><code>options.server</code>: Initialize the replication on the
     * server. The response is the CouchDB <code>POST _replicate</code> response
     * and is different from the PouchDB replication response. Also,
     * <code>options.onChange</code> is not supported on server replications.</li>
     * <li><code>options.create_target</code>: Create target database if it does
     * not exist. Only for server replications.</li>
     * </ul>
     * 
     * <h4>Example Usage:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">PouchDB</span><span class="p">.</span><span class="nx">replicate</span><span class="p">(</span><span class="s1">&#39;mydb&#39;</span><span class="p">,</span> <span class="s1">&#39;http://localhost:5984/mydb&#39;</span><span class="p">,</span> <span class="p">{</span>
     *   <span class="nx">onChange</span><span class="o">:</span> <span class="nx">onChange</span><span class="p">,</span>
     *   <span class="nx">complete</span><span class="o">:</span> <span class="nx">onComplete</span>
     * <span class="p">});;</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * There are also shorthands for replication given existing PouchDB objects,
     * these behave the same as <code>PouchDB.replicate()</code>:
     * </p>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">to</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * <span class="c1">// or</span>
     * <span class="nx">db</span><span class="p">.</span><span class="nx">replicate</span><span class="p">.</span><span class="nx">from</span><span class="p">(</span><span class="nx">remoteDB</span><span class="p">,</span> <span class="p">[</span><span class="nx">options</span><span class="p">]);</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <h4>Example Response:</h4>
     * 
     * <div class="highlight">
     * 
     * <pre>
     * <code class="js"><span class="p">{</span>
     *   <span class="s1">&#39;ok&#39;</span><span class="o">:</span> <span class="kc">true</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_read&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;docs_written&#39;</span><span class="o">:</span> <span class="mi">2</span><span class="p">,</span>
     *   <span class="s1">&#39;start_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span><span class="p">,</span>
     *   <span class="s1">&#39;end_time&#39;</span><span class="o">:</span> <span class="s2">&quot;Sun Sep 23 2012 08:14:45 GMT-0500 (CDT)&quot;</span>
     * <span class="p">}</span>
     * </code>
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * Note that the response for server replications (via
     * <code>options.server</code>) is slightly different. See the <a
     * href="http://wiki.apache.org/couchdb/Replication">CouchDB Wiki</a>.
     * </p>
     * 
     * @see <a href=
     *      'http://pouchdb.com/api.html#replication'>http://pouchdb.com/api.html#replication</a
     *      >
     * @param remoteDB
     * @param options
     * @param complete
     */
    public void replicateFrom(String remoteDB) {
        replicateFrom(remoteDB, null);
    }

    private void loadAction(String action, Map<String, Object> options, Callback<?> callback) {
        loadAction(action, null, options, callback, null);
    }

    private void loadAction(String action, String arg1, Map<String, Object> options, Callback<?> callback) {
        loadAction(action, arg1, options, callback, null);
    }

    private void loadAction(String action, String arg1, Map<String, Object> options, Callback<?> callback,
            String callbackOptionKey) {
        log.d("loadAction(%s, %s, %s, %s, %s", action, arg1, options, callback, callbackOptionKey);

        List<CharSequence> arguments = new LinkedList<CharSequence>();
        if (!TextUtils.isEmpty(arg1)) {
            arguments.add(arg1);
        }
        if (callbackOptionKey != null) {
            // callback is an option, encode it properly in the options map
            // TODO: this is hacky; do it properly
            options = options == null ? new LinkedHashMap<String, Object>()
                    : new LinkedHashMap<String, Object>(options);
            options.remove(callbackOptionKey);
            arguments.add(new StringBuilder(JsonUtil.simpleMap(options)).insert(
                    1,
                    new StringBuilder()
                            // insert after open brace
                            .append(JsonUtil.simpleString(callbackOptionKey)).append(":")
                            .append(createFunctionForCallback(callback))
                            .append(options.isEmpty() ? "" : ",")
                            ));

        } else {
            // callback is an arg, not an option in the map
            if (options != null && !options.isEmpty()) {
                arguments.add(JsonUtil.simpleMap(options));
            }
            if (callback != null) {
                arguments.add(createFunctionForCallback(callback));
            }
        }

        StringBuilder js = new StringBuilder("CouchDroid.pouchDBs[").append(id).append("].").append(action).append("(")
                .append(TextUtils.join(",", arguments)).append(");");

        runtime.loadJavascript(js);
    }

    @SuppressWarnings("rawtypes")
    private CharSequence createFunctionForCallback(final Callback innerCallback) {

        if (innerCallback == null) {
            // user doesn't give a shit
            return "function(){}";
        }

        // begin cone of death
        int callbackId = PouchJavascriptInterface.INSTANCE.addCallback(new Callback<Object>() {

            @Override
            public void onCallback(final PouchError err, final Object info) {
                Activity activity = runtime.getActivity();
                if (activity != null) {
                    // ensure it runs on the ui thread
                    activity.runOnUiThread(new Runnable() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public void run() {
                            try {
                                innerCallback.onCallback(err, info);
                            } catch (Exception e) {
                                log.e(e, "User-created callback threw an exception");
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }

            @Override
            public Object getPrimaryClass() {
                return innerCallback.getPrimaryClass();
            }

            @Override
            public Class<?> getGenericClass() {
                return innerCallback.getGenericClass();
            }
        });

        return new StringBuilder("function(err, info){PouchJavascriptInterface.callback(").append(callbackId).append(
                ", err ? JSON.stringify(err) : null, info ? JSON.stringify(info) : null);}");
    }

    /**
     * A generic callback for interacting with PouchDB.
     * 
     * @author nolan
     * 
     */
    public static interface Callback<E> {

        /**
         * Callback method, which runs on the UI thread.
         * 
         * @param err
         *            if null, there was no error
         * @param info
         *            contains additional information given by PouchDB
         */
        public void onCallback(PouchError err, E info);

        public Object getPrimaryClass();

        public Class<?> getGenericClass();
    }

    public static abstract class BulkCallback implements Callback<List<PouchInfo>> {
        public Object getPrimaryClass() {

            return new TypeReference<List<PouchInfo>>() {
            };
        }

        public Class<?> getGenericClass() {
            return null;
        }
    }

    public static abstract class AllDocsCallback<T extends PouchDocument> implements Callback<AllDocsInfo<T>> {

        @Override
        public Object getPrimaryClass() {
            return AllDocsInfo.class;
        }

        public Class<?> getGenericClass() {
            return null; // overridden
        }

    }

    public static abstract class StandardCallback implements Callback<PouchInfo> {
        public Object getPrimaryClass() {

            return PouchInfo.class;
        }

        public Class<?> getGenericClass() {
            return null;
        }
    }

    public static abstract class GetCallback<T> implements Callback<T> {

        public GetCallback() {
        }

        public Object getPrimaryClass() {
            return null; // overridden
        }

        public Class<?> getGenericClass() {
            return null;
        }
    }

    public static abstract class ReplicateCallback implements Callback<ReplicateInfo> {

        @Override
        public Object getPrimaryClass() {
            return ReplicateInfo.class;
        }

        @Override
        public Class<?> getGenericClass() {
            return null;
        }
    }
}
