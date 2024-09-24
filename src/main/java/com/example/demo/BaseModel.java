package com.example.demo;

import java.time.Instant;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseModel {

    @Id
    protected long id;

    @Version
    protected long version;

    @WhenCreated
    protected Instant createdOn;

    @WhenModified
    protected Instant modifiedOn;

}