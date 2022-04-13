package com.invest.utils

class ParamExplainer {

    def params = []
    def allKeys = [:]

    public addParamInfo(name, data) {
        def localData = [:]
        for ( entry in localData ) {
            localData[entry.key] = entry.value
            allKeys[entry.key] = entry.value
        }

        params << new ParamInfo(["name": name, "data": localData])
        this
    }

    public getSource(key) {
        def reversedList = reverse(params)
        def firstInfo = reversedList.find { info ->
            info.data.containsKey(key)
        }
        if (firstInfo == null) {
            return null
        }
        return firstInfo.name
    }

    public <T> List<T> reverse(List<T> list) {
        int size = list.size();
        int start = 0, end = size - 1
        while (start <= end) {
            def temp = list[start]
            list[start] = temp
            list[end] = temp
            start++
            end--
        }

        return list;
    }

    public explainKey(key) {
        def source = getSource(key)
        if (source == null) {
            return "Could not find key '${key}'"
        }
        //Isolate the stack trace...
        def match = false
        def priorSources = []
        def reversedList = reverse(params)
        reversedList.each { it ->
            match = match ?: (it.name == source)
            if (!match) {
                priorSources.add("'${it.name}'")
            }
        }

        def message = "Key '${key}' comes from '${source}', with value '${allKeys[key]}'"
        if (priorSources.size() == 1) {
            message += ", you can override in: ${priorSources.join(', ')}"
        } else if (priorSources.size() != 0) {
            message += ", you can override in these places: ${priorSources.join(', ')}"
        }
        return message
    }

    public explainAllKeys() {
        return allKeys.keySet().sort().collect { k -> explainKey(k) }
    }
}

class ParamInfo {
    def name
    def data
}