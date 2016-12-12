package actors;

public class Item {

    private String name;
    private int score;

    public Item(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void increment() {
        this.score = this.score + 1;
    }

    public void decrement() {
        if (this.score > 0) {
            this.score = this.score - 1;
        }
    }
}
