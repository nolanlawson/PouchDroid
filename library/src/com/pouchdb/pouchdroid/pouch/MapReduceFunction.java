package com.pouchdb.pouchdroid.pouch;


public class MapReduceFunction<T extends PouchDocumentInterface> {

    private MapFunction<T> map;
    private ReduceFunction reduce;
    private Class<T> documentClass;
    
    public MapReduceFunction(Class<T> documentClass, MapFunction<T> map, ReduceFunction reduce) {
        this.map = map;
        this.reduce = reduce;
        this.documentClass = documentClass;
    }

    public MapFunction<T> getMap() {
        return map;
    }
    public ReduceFunction getReduce() {
        return reduce;
    }
    public Class<T> getDocumentClass() {
        return documentClass;
    }
}
