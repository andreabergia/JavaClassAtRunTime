# JavaClassAtRunTime

This is a sample project of how to generate dynamically a class at runtime, 
load it, and instantiate it.

## Example problem

Suppose you have a `Map<String, Object>` read from somewhere and you want to turn
it into JSON. There are many ways to do that, but simply using 
[jackson](https://github.com/FasterXML/jackson-databind) and doing `ObjectMapper::writeValueAsString` will _not_ work.

The code in this repository will generate a Java class at runtime that _can_ be serialized 
as expected. For example, given the following:

```java
Map<String, Object> record = Map.of("name", "Andrea", "age", 37);
```

a class similar to this will be generated:

```java
class SomeInternalName {
    private final String name;
    private final Int age;
    
    SomeInternalName(Map<String, Object> record) {
        this.name = record.get("name");
        this.age = record.get("age");
    }
    
    // TODO: getters are not yet implemented
    public String getName() { 
        return name;
    }
    
    public Int getAge() {
        return age;
    }
}
```

This is all done via bytecode manipulation, using the [asm](https://asm.ow2.io/) library.

It sounds like a super strange thing to do... but believe me, I had once to do something like this
in production! 😅 I will write up about it  on [my blog](https://andreabergia.com) in the future!

## Implementation

Implementation is a work in progress, there are quite some TODO yet.
