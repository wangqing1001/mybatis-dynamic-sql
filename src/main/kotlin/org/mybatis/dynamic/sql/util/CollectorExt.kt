package org.mybatis.dynamic.sql.util


fun Collection<FragmentAndParameters>.toFragmentCollector(): FragmentCollector{
    val fragments: MutableList<String> = mutableListOf()
    val parameters: MutableMap<String, Any?> = mutableMapOf()
    for(t in this){
        fragments.add(t.fragment())
        parameters.putAll(t.parameters())
    }
    return FragmentCollector(fragments,parameters)
}

fun Collection<FragmentAndParameters>.toFragmentCollector(initialFragment:FragmentAndParameters): FragmentCollector{
    val fragments: MutableList<String> = mutableListOf()
    val parameters: MutableMap<String, Any?> = mutableMapOf()
    fragments.add(initialFragment.fragment())
    parameters.putAll(initialFragment.parameters())
    for(t in this){
        fragments.add(t.fragment())
        parameters.putAll(t.parameters())
    }
    return FragmentCollector(fragments,parameters)
}

fun Collection<FieldAndValueAndParameters>.toFieldAndValueCollector(): FieldAndValueCollector{
    if(this is List){
        return FieldAndValueCollector(this)
    }
    return FieldAndValueCollector(this.toList())
}