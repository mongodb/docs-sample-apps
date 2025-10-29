package com.mongodb.samplemflix.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.bson.types.ObjectId;

/**
 * Custom serializer for MongoDB's ObjectId to convert it to a string representation.
 *
 * <p>MongoDB's ObjectId is a 12-byte unique identifier. By default, Jackson will serialize it
 * as a base64 string, but we want to use the more human-readable hex string representation.
 *
 * <p>This custom serializer teaches Jackson to convert ObjectId to a hex string when
 * writing JSON.
 */

public class ObjectIdSerializer extends StdSerializer<ObjectId> {

    public ObjectIdSerializer() {
        super(ObjectId.class);
    }

    @Override
    public void serialize(ObjectId value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeString(value.toHexString());
    }
}
