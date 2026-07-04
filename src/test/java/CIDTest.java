import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mccue.atproto.cid.CID;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CIDTest {
    @Test
    public void shaRoundTrip() throws Exception{
        var sha = MessageDigest.getInstance("sha256").digest("Hello world".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(
                sha,
                CID.fromSha256Bytes(sha).toSha256Bytes()
        );

        assertEquals(
                "bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq",
                CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq").toString()
        );

        assertEquals(
                "bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq",
                CID.fromCIDBytes(
                        CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq").toCIDBytes()
                ).toString()
        );

        assertEquals(
                "bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq",
                CID.fromSha256HexString(
                        CID.fromCIDBytes(
                        CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq").toCIDBytes()
                ).toSha256HexString()).toString()
        );
    }

    @Test
    public void serializeRoundTrip() throws Exception {
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq"));
        }

        var bais = new ByteArrayInputStream(baos.toByteArray());
        try (var ois = new ObjectInputStream(bais)) {
            assertEquals(ois.readObject(), CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq"));
        }

        bais = new ByteArrayInputStream(Files.readAllBytes(Path.of("src", "test", "java", "test.ser")));
        try (var ois = new ObjectInputStream(bais)) {
            assertEquals(ois.readObject(), CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq"));
        }
    }

    @Test
    public void jsonSerializeTest() throws Exception {
        var objectMapper = new ObjectMapper();
        assertEquals(
                "\"bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq\"",
                objectMapper.writeValueAsString(CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq"))
        );

        assertEquals(
                CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq"),
                objectMapper.readValue("\"bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq\"", CID.class)
        );
    }
}
