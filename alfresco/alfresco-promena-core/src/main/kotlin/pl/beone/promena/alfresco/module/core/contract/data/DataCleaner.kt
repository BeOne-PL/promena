package pl.beone.promena.alfresco.module.core.contract.data

import pl.beone.promena.transformer.contract.model.data.Data

interface DataCleaner {

    fun clean(datas: List<Data>)
}