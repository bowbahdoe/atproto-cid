package dev.mccue.atproto.cid;

import org.apache.commons.codec.binary.Base32;

import module java.base;

public final class CID implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Codec codec;
    private final byte[] digest;

    private CID(Codec codec, byte[] digest) {
        this.codec = codec;
        this.digest = digest;
    }

    public static CID fromUnhashedBytes(byte[] bytes) {
        try {
            return fromSha256Bytes(
                    MessageDigest.getInstance("sha256").digest(bytes)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static CID fromCIDBytes(byte[] bytes) {
        var is = new ByteArrayInputStream(bytes);
        var version = is.read();
        if (version < 0) {
            throw new IllegalArgumentException("Reached end of bytes just before version byte.");
        }
        if (version != 1) {
            throw new IllegalArgumentException("Version must be 1.");
        }

        var codec = is.read();
        if (codec < 0) {
            throw new IllegalArgumentException("Reached end of bytes just before codec byte.");
        }

        if (codec != 0x55 && codec != 0x71) {
            throw new IllegalArgumentException("Codec must be one of 0x55 or 0x71. Got " + codec);
        }

        var hashType = is.read();

        if (hashType < 0) {
            throw new IllegalArgumentException("Reached end of bytes just before hash-type byte.");
        }
        if (hashType != 0x12) {
            throw new IllegalArgumentException(
                    "Hash type must be 0x12. Got "  + hashType
            );
        }

        var hashSize = is.read();
        if (hashSize < 0) {
            throw new IllegalArgumentException("Reached end of bytes just before hash-size byte.");
        }
        if (hashSize != 32) {
            throw new IllegalArgumentException("Hash size must be 32. Got " + hashSize);
        }

        var out = new ByteArrayOutputStream();

        long written;
        try {
            written = is.transferTo(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (written != 32) {
            throw new IllegalArgumentException("Expected exactly 32 remaining bytes. Got " + written);
        }

        return new CID(
                codec == 0x55 ? Codec.RAW : Codec.DRISL,
                out.toByteArray()
        );
    }

    private static byte[] stringEncodedToByteString(String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException("String encoded CID must not be empty");
        }
        if (s.charAt(0) != 'b') {
            throw new IllegalArgumentException("String encoded CID must start with 'b'");
        }

        return new Base32().decode(s.substring(1));
    }

    public static CID fromString(String s) {
        return fromCIDBytes(stringEncodedToByteString(s));
    }

    public static CID fromSha256HexString(Codec codec, String s) {
        return fromSha256Bytes(codec, HexFormat.of().parseHex(s));
    }

    public static CID fromSha256HexString(String s) {
        return fromSha256Bytes(HexFormat.of().parseHex(s));
    }

    public static CID fromSha256Bytes(byte[] bytes) {
        return fromSha256Bytes(Codec.RAW, bytes);
    }

    public static CID fromSha256Bytes(Codec codec, byte[] bytes) {
        if (bytes.length != 32) {
            throw new IllegalArgumentException("Must have exactly 32 bytes in sha256 hash. Got " + bytes.length);
        }
        return new CID(codec, bytes);
    }

    public int version() {
        return 1;
    }

    public String hashType() {
        return "sha256";
    }

    public int hashSize() {
        return 32;
    }

    public enum Codec {
        RAW,
        DRISL
    }

    public Codec codec() {
        return codec;
    }

    private static String bytesToBase32(byte[] bytes) {
        return new Base32()
                .encodeAsString(bytes)
                .toLowerCase()
                .replace("=", "");
    }

    private static byte[] sha256BytesToCIDBytes(Codec codec, byte[] sha256Bytes) {
        if (sha256Bytes.length != 32) {
            throw new IllegalArgumentException("Must have exactly 32 bytes in sha256 hash. Got " + sha256Bytes.length);
        }
        var out = new ByteArrayOutputStream(36);
        out.write(1); // version
        out.write(switch (codec) {
            case RAW -> 0x55;
            case DRISL -> 0x71;
        });
        out.write(0x12); // sha256
        out.write(32); // Length
        out.writeBytes(sha256Bytes);
        return out.toByteArray();
    }

    @Override
    public String toString() {
        var cidBytes = sha256BytesToCIDBytes(codec, digest);
        return "b" + bytesToBase32(cidBytes);
    }

    public byte[] toCIDBytes() {
        return sha256BytesToCIDBytes(codec, digest);
    }

    public byte[] toSha256Bytes() {
        return Arrays.copyOf(digest, digest.length);
    }

    public String toSha256HexString() {
        return HexFormat.of().formatHex(digest);
    }

    @Serial
    private Object writeReplace() {
        return new Ser(toString());
    }

    @Serial
    private Object readResolve() {
        throw new IllegalStateException();
    }

    private record Ser(String value) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        Ser {
            Objects.requireNonNull(value, "value");
        }

        @Serial
        private Object readResolve() {
            return CID.fromString(value);
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CID cid)) return false;
        return toString().equals(cid.toString());
    }
}
