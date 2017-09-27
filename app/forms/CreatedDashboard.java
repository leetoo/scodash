package forms;

public class CreatedDashboard {

    private String name;
    private String readOnlyHash;
    private String writeHash;

    public CreatedDashboard() {
    }

    public CreatedDashboard(String name, String readOnlyHash, String writeHash) {
        this.name = name;
        this.readOnlyHash = readOnlyHash;
        this.writeHash = writeHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReadOnlyHash() {
        return readOnlyHash;
    }

    public void setReadOnlyHash(String readOnlyHash) {
        this.readOnlyHash = readOnlyHash;
    }

    public String getWriteHash() {
        return writeHash;
    }

    public void setWriteHash(String writeHash) {
        this.writeHash = writeHash;
    }
}
