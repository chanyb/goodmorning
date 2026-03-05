package kr.co.kworks.goodmorning.model.business_logic;

import androidx.annotation.NonNull;

import java.util.Locale;

public class PostTestResult extends PostTest {
    public String id, createdAt, empty;

    public PostTestResult(String name, String job) {
        super(name, job);
    }

    @NonNull
    @Override
    public String toString() {
        if(name == null) name = "x";
        if(job == null) job = "x";
        if(id == null) id = "x";
        if(createdAt == null) createdAt = "x";
        if(empty == null) empty = "x";
        return String.format(Locale.KOREA, "%s %s %s %s %s", name, job, id, createdAt, empty);
    }
}
