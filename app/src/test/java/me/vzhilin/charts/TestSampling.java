package me.vzhilin.charts;

import me.vzhilin.charts.data.Column;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestSampling {
    @Test
    public void test() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add((double) i);
        }
        Column column = new Column("a", data);

        System.out.println(column.sample(1, 0, 1));
        System.out.println(column.sample(2, 0, 1));
    }
}
