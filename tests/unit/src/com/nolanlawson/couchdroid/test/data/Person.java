package com.nolanlawson.couchdroid.test.data;

import java.util.List;

import com.nolanlawson.couchdroid.pouch.PouchDocument;

public class Person extends PouchDocument {

    private String name;
    private long dystopianBarcodeId;
    private int numberOfPetsOwned;
    private List<GameBoy> gameBoys;
    private boolean belieber;

    public Person() {
    }
    
    public Person(String name, long dystopianBarcodeId, int numberOfPetsOwned, List<GameBoy> gameBoys, boolean belieber) {
        this.name = name;
        this.dystopianBarcodeId = dystopianBarcodeId;
        this.numberOfPetsOwned = numberOfPetsOwned;
        this.gameBoys = gameBoys;
        this.belieber = belieber;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getDystopianBarcodeId() {
        return dystopianBarcodeId;
    }
    public void setDystopianBarcodeId(long dystopianBarcodeId) {
        this.dystopianBarcodeId = dystopianBarcodeId;
    }
    public int getNumberOfPetsOwned() {
        return numberOfPetsOwned;
    }
    public void setNumberOfPetsOwned(int numberOfPetsOwned) {
        this.numberOfPetsOwned = numberOfPetsOwned;
    }
    public boolean isBelieber() {
        return belieber;
    }
    public void setBelieber(boolean belieber) {
        this.belieber = belieber;
    }
    
    public List<GameBoy> getGameBoys() {
        return gameBoys;
    }
    public void setGameBoys(List<GameBoy> gameBoys) {
        this.gameBoys = gameBoys;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (belieber ? 1231 : 1237);
        result = prime * result + (int) (dystopianBarcodeId ^ (dystopianBarcodeId >>> 32));
        result = prime * result + ((gameBoys == null) ? 0 : gameBoys.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + numberOfPetsOwned;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Person other = (Person) obj;
        if (belieber != other.belieber)
            return false;
        if (dystopianBarcodeId != other.dystopianBarcodeId)
            return false;
        if (gameBoys == null) {
            if (other.gameBoys != null)
                return false;
        } else if (!gameBoys.equals(other.gameBoys))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (numberOfPetsOwned != other.numberOfPetsOwned)
            return false;
        return true;
    }
}
