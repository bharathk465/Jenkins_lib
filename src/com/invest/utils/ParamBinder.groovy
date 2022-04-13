package com.invest.utils

class ParamBinder {

    def static bind(params, workflow, body) {
        def service = new ParamExplainer()
        service.addParamInfo("defaults", params)
        def otherParams = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = otherParams
        def result = body(workflow)
        service.addParamInfo("provided-job-values", otherParams)
        otherParams.each { k, v -> params.put(k, v) }
        service.explainAllKeys().each { explanation ->
            workflow.echo explanation
        }
        return result
    }
}