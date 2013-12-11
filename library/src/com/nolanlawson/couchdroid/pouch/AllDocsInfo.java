package com.nolanlawson.couchdroid.pouch;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

  /**
    * Info object returned from Pouch for an "allDocs" request.  Looks like this:
    * {
    *   "total_rows": 1,
    *   "rows": [{
    *     "doc": {
    *       "_id": "0B3358C1-BA4B-4186-8795-9024203EB7DD",
    *       "_rev": "1-5782E71F1E4BF698FA3793D9D5A96393",
    *       "blog_post": "my blog post"
    *     },
    *    "id": "0B3358C1-BA4B-4186-8795-9024203EB7DD",
    *    "key": "0B3358C1-BA4B-4186-8795-9024203EB7DD",
    *    "value": {
    *     "rev": "1-5782E71F1E4BF698FA3793D9D5A96393"
    *    }
    *  }]
    * }
    * 
    * @author nolan
    *
    */
public class AllDocsInfo<T extends PouchDocumentInterface> {
    
    private int totalRows;
    private int offset;
    private List<Row<T>> rows;
    
    public int getOffset() {
        return offset;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
    @JsonProperty("total_rows")
    public int getTotalRows() {
        return totalRows;
    }

    @JsonProperty("total_rows")
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public List<Row<T>> getRows() {
        return rows;
    }

    public void setRows(List<Row<T>> rows) {
        this.rows = rows;
    }
    
    /**
     * Convenience method for getting all the documents, assuming you set {include_docs : true}.
     * @return
     */
    @JsonIgnore
    public List<T> getDocuments() {
        List<T> docs = new ArrayList<T>();
        for (Row<T> row : getRows()) {
            docs.add(row.getDoc());
        }
        return docs;
    }

    public static class Row<T extends PouchDocumentInterface> {
        private T doc;
        private String id;
        private String key;
        private RowValue value;
        
        public T getDoc() {
            return doc;
        }
        public void setDoc(T doc) {
            this.doc = doc;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public RowValue getValue() {
            return value;
        }
        public void setValue(RowValue value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return "Row [doc=" + doc + ", id=" + id + ", key=" + key + ", value=" + value + "]";
        }
    }
    
    public static class RowValue {
        private String rev;

        public String getRev() {
            return rev;
        }

        public void setRev(String rev) {
            this.rev = rev;
        }

        @Override
        public String toString() {
            return "RowValue [rev=" + rev + "]";
        }
    }

    @Override
    public String toString() {
        return "AllDocsInfo [totalRows=" + totalRows + ", rows=" + rows + "]";
    }
}
