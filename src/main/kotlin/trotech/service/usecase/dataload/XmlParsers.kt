package trotech.service.usecase.dataload

import org.w3c.dom.Element
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import trotech.dto.Layer
import trotech.dto.TileMatrix
import trotech.dto.TileMatrixSet

fun parseLayer(xml: String): Layer {
    val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val inputSource = InputSource(StringReader(xml))
    val doc = docBuilder.parse(inputSource)
    val root = doc.documentElement

    val id = root.getElementsByTagName("ows:Identifier").item(0).textContent
    val title = root.getElementsByTagName("ows:Title").item(0).textContent
    val formatNodes = root.getElementsByTagName("Format")
    val formats = (0 until formatNodes.length).map { i ->
        formatNodes.item(i).textContent
    }

    val styleNode = root.getElementsByTagName("Style").item(0) as Element
    val styleIdentifier = styleNode.getElementsByTagName("ows:Identifier").item(0).textContent

    val tileMatrixSetLinksNodes = root.getElementsByTagName("TileMatrixSetLink")
    val tileMatrixSetLinks = (0 until tileMatrixSetLinksNodes.length).map { i ->
        val linkNode = tileMatrixSetLinksNodes.item(i) as Element
        linkNode.getElementsByTagName("TileMatrixSet").item(0).textContent
    }

    return Layer(
        id = id,
        title = title,
        tileMatrixSetLinks = tileMatrixSetLinks,
        format = formats.firstOrNull() ?: "image/png", // Если несколько форматов, берем первый
        style = styleIdentifier
    )
}

fun parseTileMatrixSet(xml: String): TileMatrixSet {
    val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val inputSource = InputSource(StringReader(xml))
    val doc = docBuilder.parse(inputSource)
    val root = doc.documentElement

    val id = root.getElementsByTagName("ows:Identifier").item(0).textContent
    val supportedCRS = root.getElementsByTagName("ows:SupportedCRS").item(0).textContent

    val tileMatricesNodes = root.getElementsByTagName("TileMatrix")
    val tileMatrices = (0 until tileMatricesNodes.length).map { i ->
        val matrixElement = tileMatricesNodes.item(i) as Element

        val matrixId = matrixElement.getElementsByTagName("ows:Identifier").item(0).textContent
        val scaleDenominator = matrixElement.getElementsByTagName("ScaleDenominator").item(0).textContent.toDouble()
        val topLeftCorner = matrixElement.getElementsByTagName("TopLeftCorner").item(0).textContent
            .split(" ").map { it.toDouble() }
        val tileWidth = matrixElement.getElementsByTagName("TileWidth").item(0).textContent.toInt()
        val tileHeight = matrixElement.getElementsByTagName("TileHeight").item(0).textContent.toInt()
        val matrixWidth = matrixElement.getElementsByTagName("MatrixWidth").item(0).textContent.toInt()
        val matrixHeight = matrixElement.getElementsByTagName("MatrixHeight").item(0).textContent.toInt()

        TileMatrix(
            id = matrixId,
            scaleDenominator = scaleDenominator,
            topLeftCornerX = topLeftCorner[0],
            topLeftCornerY = topLeftCorner[1],
            tileWidth = tileWidth,
            tileHeight = tileHeight,
            matrixWidth = matrixWidth,
            matrixHeight = matrixHeight
        )
    }

    return TileMatrixSet(
        id = id,
        supportedCRS = supportedCRS,
        tileMatrices = tileMatrices
    )
}