package com.pouchdb.pouchdroid.pouch;

import java.util.List;
import java.util.Map;

import com.pouchdb.pouchdroid.pouch.callback.AllDocsCallback;
import com.pouchdb.pouchdroid.pouch.callback.BulkCallback;
import com.pouchdb.pouchdroid.pouch.callback.GetCallback;
import com.pouchdb.pouchdroid.pouch.callback.ReplicateCallback;
import com.pouchdb.pouchdroid.pouch.callback.StandardCallback;


/**
 * Superclass for {@code AsyncPouchDB}, purely to avoid having a billion lines of Javadoc mixed in with the source code.
 * 
 * <p/>Also, it's Java.  We'll eventually have an {@code AbstractPouchDBFactoryBeanFactoryImpl}.
 * 
 * @author nolan
 *
 */
public abstract class AbstractPouchDB<T extends PouchDocumentInterface> {
    
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
     * </div>
     * 
     * @see <a href='http://pouchdb.com/api.html#delete_database'>http://pouchdb.com/api.html#delete_database</a>
     */
    public abstract void destroy(Map<String, Object> options, StandardCallback callback);
    
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
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document</a>
     * @param doc
     * @param callback
     */
    public abstract void put(T doc, Map<String, Object> options, StandardCallback callback);
    
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
     *      'http://pouchdb.com/api.html#create_document'>http://pouchdb.com/api.html#create_document</a>
     * @param doc
     * @param callback
     */

    public abstract void post(T doc, Map<String, Object> options, StandardCallback callback);
    
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
     * @see <a href='http://pouchdb.com/api.html#fetch_document'>http://pouchdb.com/api.html#fetch_document</a>
     * 
     * @param docid
     * @param options
     * @param callback
     */
    public abstract void get(String docid, Map<String, Object> options, final GetCallback<T> callback);
    
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
    public abstract void remove(T doc, Map<String, Object> options, StandardCallback callback);
    
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
    public abstract void bulkDocs(List<T> docs, Map<String, Object> options, BulkCallback callback);
    
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
    public abstract void allDocs(Map<String, Object> options, final AllDocsCallback<T> callback);
    
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
    public abstract void replicateTo(String remoteDB, Map<String, Object> options, ReplicateCallback complete);
 
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
    public abstract void replicateFrom(String remoteDB, Map<String, Object> options, ReplicateCallback complete);
}
