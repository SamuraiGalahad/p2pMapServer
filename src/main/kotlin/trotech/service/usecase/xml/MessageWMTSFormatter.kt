package trotech.service.usecase.xml


import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult



/**
 * Класс для работы с WMTS Capabilities xml
 */

open class MessageWMTSFormatter {

    /**
     * Парсинг слоя в объект DTO для сохранения в базу данных
     * @param xml -- строка, которая представляет из себя xmll разметку
     * слоя
     */
    fun parseLayer(xml: String): Layer {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(xml.byteInputStream())

        val title = document.getElementsByTagName("ows:Title").item(0).textContent
        val identifier = document.getElementsByTagName("ows:Identifier").item(0).textContent
        val lowerCorner = document.getElementsByTagName("ows:LowerCorner").item(0).textContent
        val upperCorner = document.getElementsByTagName("ows:UpperCorner").item(0).textContent
        val formats = document.getElementsByTagName("Format").let { nodes ->
            (0 until nodes.length).map { nodes.item(it).textContent }
        }
        val tileMatrixSets = document.getElementsByTagName("TileMatrixSet").let { nodes ->
            (0 until nodes.length).map { nodes.item(it).textContent }
        }

        return Layer(
            title = title,
            identifier = identifier,
            boundingBox = BoundingBox(lowerCorner, upperCorner),
            formats = formats,
            tileMatrixSets = tileMatrixSets
        )
    }


    /**
     *  Генерация Capabilities для выдачи информации клиентам
     *  @param layer -- слой (карта)
     */
    fun generateCapabilitiesXml(layer: Layer): String {
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val doc = docBuilder.newDocument()

        val capabilities = doc.createElement("Capabilities")
        capabilities.setAttribute("version", "1.0.0")
        doc.appendChild(capabilities)

        val contents = doc.createElement("Contents")
        capabilities.appendChild(contents)

        val layerElement = doc.createElement("Layer")
        contents.appendChild(layerElement)

        val titleElement = doc.createElement("ows:Title")
        titleElement.textContent = layer.title
        layerElement.appendChild(titleElement)

        val identifierElement = doc.createElement("ows:Identifier")
        identifierElement.textContent = layer.identifier
        layerElement.appendChild(identifierElement)

        val bboxElement = doc.createElement("ows:WGS84BoundingBox")
        val lowerCornerElement = doc.createElement("ows:LowerCorner")
        lowerCornerElement.textContent = layer.boundingBox.lowerCorner
        val upperCornerElement = doc.createElement("ows:UpperCorner")
        upperCornerElement.textContent = layer.boundingBox.upperCorner
        bboxElement.appendChild(lowerCornerElement)
        bboxElement.appendChild(upperCornerElement)
        layerElement.appendChild(bboxElement)

        layer.formats.forEach { format ->
            val formatElement = doc.createElement("Format")
            formatElement.textContent = format
            layerElement.appendChild(formatElement)
        }

        layer.tileMatrixSets.forEach { matrixSet ->
            val tileMatrixSetLink = doc.createElement("TileMatrixSetLink")
            val tileMatrixSet = doc.createElement("TileMatrixSet")
            tileMatrixSet.textContent = matrixSet
            tileMatrixSetLink.appendChild(tileMatrixSet)
            layerElement.appendChild(tileMatrixSetLink)
        }

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        val source = DOMSource(doc)
        val writer = java.io.StringWriter()
        val result = StreamResult(writer)
        transformer.transform(source, result)
        return writer.toString()
    }

}