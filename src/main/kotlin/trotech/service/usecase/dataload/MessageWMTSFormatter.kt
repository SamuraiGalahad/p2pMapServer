package trotech.service.usecase.dataload

import trotech.dao.database.metadata.loadAllLayers
import trotech.dao.database.metadata.loadAllTileMatrixSets
import trotech.dto.Layer
import trotech.dto.TileMatrixSet



class MessageWMTSFormatter(
    val title: String,
    val abstract: String,
    val serviceType: String,
    val serviceVersion: String,
    val serviceProvicderName: String,
    val serviceProviderUrl: String
) {

    fun getTileLayers() : List<Layer> {
        return loadAllLayers()
//        return listOf()
    }

    fun getTileMatrixSets() : List<TileMatrixSet> {
        return loadAllTileMatrixSets()
//        return listOf()
    }

    fun getCapabilitiesGen() : String {
        return """
        <?xml version="1.0" encoding="UTF-8"?>
        <Capabilities xmlns="http://www.opengis.net/wmts/1.0"
                      xmlns:ows="http://www.opengis.net/ows/1.1"
                      xmlns:xlink="http://www.w3.org/1999/xlink"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://www.opengis.net/wmts/1.0
                      http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd"
                      version="1.0.0">

            <ows:ServiceIdentification>
                <ows:Title>${title}</ows:Title>
                <ows:Abstract>${abstract}</ows:Abstract>
                <ows:ServiceType>${serviceType}</ows:ServiceType>
                <ows:ServiceTypeVersion>${serviceVersion}</ows:ServiceTypeVersion>
            </ows:ServiceIdentification>

            <ows:ServiceProvider>
                <ows:ProviderName>${serviceProvicderName}</ows:ProviderName>
                <ows:ProviderSite xlink:href="${serviceProviderUrl}"/>
            </ows:ServiceProvider>
            <Contents>
                ${getTileLayers().joinToString("\n") { layer ->
            """
                    <Layer>
                        <ows:Title>${layer.title}</ows:Title>
                        <ows:Identifier>${layer.id}</ows:Identifier>
                        <Style isDefault="true">
                            <ows:Identifier>${layer.style}</ows:Identifier>
                        </Style>
                        <Format>${layer.format}</Format>
                        ${layer.tileMatrixSetLinks.joinToString("\n") { setLink ->
                """
                            <TileMatrixSetLink>
                                <TileMatrixSet>$setLink</TileMatrixSet>
                            </TileMatrixSetLink>
                            """.trimIndent()
            }}
                    </Layer>
                    """.trimIndent()
        }}
                
                ${getTileMatrixSets().joinToString("\n") { tms ->
            """
                    <TileMatrixSet>
                        <ows:Identifier>${tms.id}</ows:Identifier>
                        <ows:SupportedCRS>${tms.supportedCRS}</ows:SupportedCRS>
                        ${tms.tileMatrices.joinToString("\n") { matrix ->
                """
                            <TileMatrix>
                                <ows:Identifier>${matrix.id}</ows:Identifier>
                                <ScaleDenominator>${matrix.scaleDenominator}</ScaleDenominator>
                                <TopLeftCorner>${matrix.topLeftCornerX} ${matrix.topLeftCornerY}</TopLeftCorner>
                                <TileWidth>${matrix.tileWidth}</TileWidth>
                                <TileHeight>${matrix.tileHeight}</TileHeight>
                                <MatrixWidth>${matrix.matrixWidth}</MatrixWidth>
                                <MatrixHeight>${matrix.matrixHeight}</MatrixHeight>
                            </TileMatrix>
                            """.trimIndent()
            }}
                    </TileMatrixSet>
                    """.trimIndent()
        }}
            </Contents>
        </Capabilities>
    """.trimIndent()
    }
}