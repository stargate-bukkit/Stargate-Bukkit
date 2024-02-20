package net.knarcraft.stargate.container;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RelativeBlockVectorTest {

    @Test
    public void addToVectorTest() {
        int right = 5;
        int down = 5;
        int out = 3;

        RelativeBlockVector relativeBlockVector = new RelativeBlockVector(right, down, out);

        for (int i = 0; i < 1000; i++) {
            int randomValue = getRandomNumber();
            RelativeBlockVector newVector = relativeBlockVector.addRight(randomValue);
            Assertions.assertEquals(new RelativeBlockVector(right + randomValue, down, out), newVector);

            newVector = relativeBlockVector.addOut(randomValue);
            Assertions.assertEquals(new RelativeBlockVector(right, down, out + randomValue), newVector);

            newVector = relativeBlockVector.addDown(randomValue);
            Assertions.assertEquals(new RelativeBlockVector(right, down + randomValue, out), newVector);
        }
    }

    @Test
    public void invertTest() {
        for (int i = 0; i < 1000; i++) {
            int randomNumber1 = getRandomNumber();
            int randomNumber2 = getRandomNumber();
            int randomNumber3 = getRandomNumber();

            RelativeBlockVector relativeBlockVector = new RelativeBlockVector(randomNumber1, randomNumber2,
                    randomNumber3);
            RelativeBlockVector invertedBlockVector = new RelativeBlockVector(-randomNumber1, -randomNumber2,
                    -randomNumber3);
            Assertions.assertEquals(invertedBlockVector, relativeBlockVector.invert());
        }
    }

    /**
     * Gets a random number between -500 and 500
     *
     * @return <p>A random number between -500 and 500</p>
     */
    private int getRandomNumber() {
        return (int) ((Math.random() - 0.5) * 1000);
    }

}
