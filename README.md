# CID

[CIDs](https://atproto.com/specs/data-model) (content identifiers) as used by ATProto.

```xml
<dependency>
    <groupId>dev.mccue</groupId>
    <artifactId>atproto-cid</artifactId>
    <version>2026.07.03</version>
</dependency>
```

Provides a single class which allows for representing a CID directly in code. 

You can obtain a CID from its string representation.

```java
var cid = CID.fromString("bafkreifozdmpkf2slviaknu6zlzyupmcakwybxth37kaxqndpw56ghejfq");
```

Or from various other factory methods.

```java
CID.fromSha256Bytes(...);
CID.fromSha256HexString(...);
CID.fromUnhashedBytes(...);
```

CIDs can be serialized using `toString()` as well as Jackson or the `Serializable` mechanism.