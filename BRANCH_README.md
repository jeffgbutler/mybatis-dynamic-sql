# Parameter Bindings

This branch implements a new rendering method based on parameter bindings, rather than just placing parameters in a Map.
This has the following benefits:

1. It would allow a "raw JDBC" rendering strategy
2. It would allow for "raw R2DBC" rendering - we might need to add an optional NullType to Bindable Column
3. The ParameterBinding caries type information as well as the basic value of a parameter

Drawbacks:

1. It is a breaking change if anyone has implemented complex functions that interact with FragmentAndParameters
2. We don't **guarantee** that the bindings are in the proper order for RAW JDBC - although they are in the proper order
   for the current version of the library. Not sure if we need to add additional function to guarantee the order, or
   just ensuring it works through tests is sufficient
3. The ParameterBindings object holds the list of parameter bindings, and also implements Map<String, Object> so it can
   be used for a MyBatis and Spring parameter map. There might be a slight performance hit for this.
