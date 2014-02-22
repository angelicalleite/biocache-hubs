package au.org.ala.biocache.hubs

import groovy.xml.MarkupBuilder
import org.apache.commons.lang.StringUtils

class OccurrenceTagLib {
    //static defaultEncodeAs = 'html'
    //static encodeAsForTags = [tagName: 'raw']
    static namespace = 'alatag'     // namespace for headers and footers

    /**
     * Formats the display of dynamic facet names in Sandbox (facet options popup)
     *
     * @attr fieldName REQUIRED the field name
     */
    def formatDynamicFacetName = { attrs ->
        def fieldName = attrs.fieldName
        def output
        if (fieldName.endsWith('_s') || fieldName.endsWith('_i') || fieldName.endsWith('_d')) {
            output = fieldName.substring(0,-2).replaceAll("_", " ")
        } else if (fieldName.endsWith('_RNG')) {
            output = fieldName.substring(0,-4).replaceAll("_", " ") + " (range)"
        } else {
            output = "${g:message(code:"facet.${fieldName}", default: fieldName)}"
        }
        out << output
    }

    /**
     * Format scientific name for HTML display
     *
     * @attr rankId REQUIRED
     * @attr name REQUIRED
     * @attr acceptedName
     */
    def formatSciName = { attrs ->
        def name = attrs.name
        def acceptedName = attrs.acceptedName
        def rankId = attrs.rankId.toInteger()
        def acceptedNameOutput = ""
        def nameOutput = ""
        def ital = ["",""]

        if (!rankId || rankId >= 6000) {
            ital = ["<i>","</i>"]
        }

        if (acceptedName) {
            acceptedNameOutput = " (accepted name: ${ital[0]}${acceptedName}${ital[1]})"
        }

        nameOutput = "${ital[0]}${name}${ital[1]}${acceptedNameOutput}"

        out << nameOutput.trim()
    }

    /**
     * Output the appropriate raw scientific name for the record
     *
     * @attr occurrence REQUIRED the occurrence record (jsonObject)
     */
    def rawScientificName = { attrs ->
        def rec = attrs.occurrence
        def name

        if (rec.raw_scientificName) {
            name = rec.raw_scientificName
        } else if (rec.species) {
            name = rec.species
        } else if (rec.genus) {
            name = rec.genus
        } else if (rec.family) {
            name = rec.family
        } else if (rec.order) {
            name = rec.order
        } else if (rec.phylum) {
            name = rec.phylum
        }  else if (rec.kingdom) {
            name = rec.kingdom
        } else {
            name = g.message(code:"record.noNameSupplied", default: "No name supplied")
        }

        out << name
    }

    /**
     * Generate HTML for current filters
     *
     * @attr item REQUIRED
     */
    def currentFilterItem = { attrs ->
        def item = attrs.item
        def filterLabel = alatag.formatDynamicFacetName(fieldName: item.value.displayName)
        def fqLabel = (filterLabel.startsWith('-')) ? "<span class=\"red\">[exclude]</span> ${filterLabel.substring(1, filterLabel.size())}" : filterLabel

        def mb = new MarkupBuilder(out)
        mb.span(class:'activeFq') {
            mkp.yield(message(code: fqLabel, default: fqLabel))
        }
        mb.span() { mkp.yieldUnescaped("&nbsp;") }
        mb.a(
                href:"#",
                class: "btn btn-mini btn-primary removeLink",
                title: "remove filter",
                "data-facet":"${item.key}:${item.value.value.encodeAsURL()}",
                onClick:"removeFacet(this); return false;"
        ) {
            i(class:"icon-remove icon-white", style:"margin-left:5px", "")
        }
    }

    /**
     * Generate facet links in the left hand column
     *
     * @attr fieldResult REQUIRED
     * @attr facetResult REQUIRED
     * @attr queryParam REQUIRED
     */
    def facetLinkItems = { attrs ->
        def fieldResult = attrs.fieldResult
        def facetResult = attrs.facetResult
        def queryParam = attrs.queryParam
        def mb = new MarkupBuilder(out)
        def fqValue = fieldResult.label?.encodeAsURL()

        def addCounts = { count ->
            mb.span(class:"facetCount") {
                mkp.yieldUnescaped(" (")
                mkp.yield(g.formatNumber(number: "${count}", format:"#,###,###"))
                mkp.yieldUnescaped(")")
            }
        }

        // Catch specific facets fields
        if (fieldResult.fq) {
            // biocache-service has provided a fq field in the fieldResults list
            mb.li {
                a(href:"?${queryParam}&fq=${fieldResult.fq?.encodeAsURL()}") {
                    mkp.yield(message(code:"${fieldResult.label?:'unknown'}", default:"${fieldResult.label}"))
                }
                addCounts(fieldResult.count)
            }
        } else if (StringUtils.startsWith(facetResult.fieldName, "occurrence_") && StringUtils.endsWith(fieldResult.label, "Z")) {
            // decade year ranges
            def startYear = fieldResult.label.substring(0, 4)
            def endDate = fieldResult.label.replace('0-01-01T00:00:00Z','9-12-31T11:59:59Z')
            def endYear = endDate.substring(0, 4)

            mb.li {
                a(href:"?${queryParam}&fq=${facetResult.fieldName}:[${fieldResult.label} TO ${endDate}]") {
                    mkp.yieldUnescaped("${startYear} - ${endYear}")
                }
                addCounts(fieldResult.count)
            }
        } else {
            def label = g.message(code:"${facetResult.fieldName}.${fieldResult.label}", default:"")?:
                    g.message(code:"${fieldResult.label?:'unknown'}", default:"${fieldResult.label}")
            mb.li {
                a(href:"?${queryParam}&fq=${facetResult.fieldName}:%22${fqValue}%22") {
                    mkp.yield(label)
                }
                addCounts(fieldResult.count)
            }
        }
    }

    /**
     * Determine the recordId TODO
     *
     * @attr record REQUIRED the record object (JsonObject)
     * @attr skin
     */
    def getRecordId = { attrs ->
//        <c:when test="${skin == 'avh'}">
//            <c:set var="recordId" value="${record.raw.occurrence.catalogNumber}"/>
//        </c:when>
//        <c:when test="${not empty record.raw.occurrence.collectionCode && not empty record.raw.occurrence.catalogNumber}">
//            <c:set var="recordId" value="${record.raw.occurrence.collectionCode} - ${record.raw.occurrence.catalogNumber}"/>
//        </c:when>
//        <c:when test="${not empty record.processed.attribution.dataResourceName && not empty record.raw.occurrence.catalogNumber}">
//            <c:set var="recordId" value="${record.processed.attribution.dataResourceName} - ${record.raw.occurrence.catalogNumber}"/>
//        </c:when>
//        <c:when test="${not empty record.raw.occurrence.occurrenceID}">
//            <c:set var="recordId" value="${record.raw.occurrence.occurrenceID}"/>
//        </c:when>
//        <c:otherwise>
//            <c:set var="recordId" value="${record.raw.uuid}"/>
//        </c:otherwise>
        def record = attrs.record?:null
        def skin = attrs.skin?:"ala"
        def recordId = record.raw.uuid

        if (skin == 'avh') {
            recordId = record.raw.occurrence.catalogNumber
        } else if (record.raw.occurrence.collectionCode && record.raw.occurrence.catalogNumber) {
            recordId = record.raw.occurrence.collectionCode + " - " + record.raw.occurrence.catalogNumber
        } else if (record.processed.attribution.dataResourceName && record.raw.occurrence.catalogNumber) {
            recordId = record.processed.attribution.dataResourceName + " - " + record.raw.occurrence.catalogNumber
        } else if (record.raw.occurrence.occurrenceID) {
            recordId = record.raw.occurrence.occurrenceID
        }

        out << recordId
    }

    /**
     * Determine the scientific name
     *
     * @attr record REQUIRED the record object (JsonObject)
     */
    def getScientificName = { attrs ->
//        <c:choose>
//            <c:when test="${not empty record.processed.classification.scientificName}">
//                    ${record.processed.classification.scientificName} ${record.processed.classification.scientificNameAuthorship}
//            </c:when>
//            <c:when test="${not empty record.raw.classification.scientificName}">
//                ${record.raw.classification.scientificName} ${record.raw.classification.scientificNameAuthorship}
//            </c:when>
//            <c:otherwise>
//                    ${record.raw.classification.genus} ${record.raw.classification.specificEpithet}
//            </c:otherwise>
//        </c:choose>
        def record = attrs.record
        out << "${record?.raw?.classification?.genus} ${record?.raw?.classification?.specificEpithet}"
    }

    /**
     * TODO
     *
     * @attr groupedAssertions REQUIRED
     */
    def groupedAssertions = { attrs ->
        def groupedAssertions = attrs.groupedAssertions
        out << "${groupedAssertions} TODO"
    }

    /**
     * Generate the icon and popup for the data quality help codes/linsks
     *
     * @attr code REQUIRED
     */
    def dataQualityHelp = { attrs ->
        def mb = new MarkupBuilder(out)
        mb.a(
                href: "#",
                class:"dataQualityHelpLink",
                "data-toggle":"popover",
                "data-code": attrs.code?:""
        ) {
            i(class:"icon-question-sign", "")
        }
        //def html = "&nbsp;<a href='#' class='dataQualityHelpLink' data-toggle='popover' data-code='${code}'><i class='icon-question-sign'></i></a>"
        //out << html
    }

    /**
     * Generate the table body for the raw vs processed table (popup)
     *
     * @attr map REQUIRED
     */
    def formatRawVsProcessed = { attrs ->
        def map = attrs.map
        def mb = new MarkupBuilder(out)

        map.each { group ->
            if (group.value) {
                group.value.eachWithIndex() { field, i ->
                    mb.tr() {
                        if (i == 0) {
                            td(class:"noStripe", rowspan:"${group.value.length()}", group.key)
                        }
                        td(alatag.camelCaseToHuman(text: field.name))
                        td(field.raw)
                        td(field.processed)
                    }
                }
            }
        }
    }

    /**
     * Camel case converted, taken from JS code:
     *
     * str.replace(/([a-z])([A-Z])/g, "$1 $2").toLowerCase().capitalize();
     *
     * @attr text REQUIRED the input text
     */
    def camelCaseToHuman = { attrs ->
        String text = attrs.text
        out << text.replaceAll(/([a-z])([A-Z])/, '$1 $2').toLowerCase().capitalize()
    }

    /**
     * Generate an occurrence table row
     *
     * @attr fieldName REQUIRED
     * @attr fieldNameIsMsgCode
     * @attr fieldCode
     * @attr section REQUIRED
     * @attr annotate REQUIRED
     * @attr path
     * @attr guid
     */
    def occurrenceTableRow = { attrs, body ->
        String bodyText = (String) body()
        def guid = attrs.guid
        def path = attrs.path
        def fieldCode = attrs.fieldCode
        def fieldName = attrs.fieldName
        def fieldNameIsMsgCode = attrs.fieldNameIsMsgCode

        if (StringUtils.isNotBlank(bodyText)) {
            def link = (guid) ? "${path}${guid}" : ""
            def mb = new MarkupBuilder(out)

            mb.tr(id:"${fieldCode}") {
                td(class:"dwcLabel") {
                    if (fieldNameIsMsgCode) {
                        mkp.yield(g.message(code: "${fieldName}", default :"${fieldName}"))
                    } else {
                        mkp.yieldUnescaped(fieldName)
                    }

                }
                td(class:"value") {
                    if (link) {
                        a(href: link) {
                            mkp.yieldUnescaped(bodyText)
                        }
                    } else {
                        mkp.yieldUnescaped(bodyText)
                    }
                }
            }
        }
    }

    /**
     * Generate a compare record "row"
     *
     * @attr compareRecord REQUIRED
     * @attr fieldsMap REQUIRED
     * @attr group REQUIRED
     * @attr exclude REQUIRED
     */
    def formatExtraDwC = { attrs ->
        def compareRecord = attrs.compareRecord
        Map fieldsMap = attrs.fieldsMap
        def group = attrs.group
        def exclude = attrs.exclude
        def output = ""
        def mb = new MarkupBuilder(out)

        compareRecord.get(group).each { cr ->
            def key = cr.name
            def label = g.message(code:key, default:"")?:alatag.camelCaseToHuman(text: key)?:StringUtils.capitalize(key)

            // only output fields not already included (by checking fieldsMap Map) && not in excluded list
            if (!fieldsMap.containsKey(key) && !StringUtils.containsIgnoreCase(exclude, key)) {
                //def mb = new MarkupBuilder(out)
                def tagBody

                if (cr.processed && cr.raw && cr.processed == cr.raw) {
                    tagBody = cr.processed
                } else if (!cr.raw && cr.processed) {
                    tagBody = cr.processed
                } else if (cr.raw && !cr.processed) {
                    tagBody = cr.raw
                } else {
                    tagBody = "${cr.processed} <br/><span class='originalValue'>Supplied as ${cr.raw}</span>"
                }
                output += alatag.occurrenceTableRow(annotate:"true", section:"dataset", fieldCode:"${key}", fieldName:"<span class='dwc'>${label}</span>") {
                    tagBody
                }
            }
        }

        out << output
    }


}
