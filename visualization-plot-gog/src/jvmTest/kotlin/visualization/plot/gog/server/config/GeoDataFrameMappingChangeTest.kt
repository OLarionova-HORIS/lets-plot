package jetbrains.datalore.visualization.plot.gog.server.config

import jetbrains.datalore.visualization.plot.gog.config.GeoPositionsDataUtil.DATA_COLUMN_JOIN_KEY
import jetbrains.datalore.visualization.plot.gog.config.GeoPositionsDataUtil.MAP_COLUMN_JOIN_KEY
import jetbrains.datalore.visualization.plot.gog.config.GeoPositionsDataUtil.MAP_COLUMN_REGION
import jetbrains.datalore.visualization.plot.gog.config.GeoPositionsDataUtil.MAP_COLUMN_REQUEST
import jetbrains.datalore.visualization.plot.gog.config.Option.Geom.Choropleth.GEO_POSITIONS
import jetbrains.datalore.visualization.plot.gog.config.Option.GeomName
import jetbrains.datalore.visualization.plot.gog.config.Option.Layer.DATA
import jetbrains.datalore.visualization.plot.gog.config.Option.Layer.GEOM
import jetbrains.datalore.visualization.plot.gog.config.Option.Layer.MAPPING
import jetbrains.datalore.visualization.plot.gog.config.Option.Layer.POS
import jetbrains.datalore.visualization.plot.gog.config.Option.Layer.STAT
import jetbrains.datalore.visualization.plot.gog.config.Option.Meta.DATA_META
import jetbrains.datalore.visualization.plot.gog.config.Option.Meta.GeoDataFrame
import jetbrains.datalore.visualization.plot.gog.config.Option.Meta.MAP_DATA_META
import jetbrains.datalore.visualization.plot.gog.core.render.Aes
import jetbrains.datalore.visualization.plot.gog.server.config.ServerSideTestUtil.createLayerConfigsByLayerSpec
import jetbrains.datalore.visualization.plot.gog.server.config.ServerSideTestUtil.geoPositionsDict
import jetbrains.datalore.visualization.plot.gog.server.config.SingleLayerAssert.Companion.assertThat
import java.util.*
import kotlin.test.Test

class GeoDataFrameMappingChangeTest {

    @Test
    fun whenDataContainsGeoDataFrame_shouldMoveGeometryColumnToMap() {
        val cfg = createLayerConfigsByLayerSpec(mapOf(
                GEOM to GeomName.POLYGON,
                DATA to GEO_DATA_FRAME_WITH_IDS,
                DATA_META to GEO_DATA_FRAME_META,
                GEO_POSITIONS to null,
                MAP_DATA_META to null,
                MAPPING to mapOf(Aes.FILL.name to NAME_COLUMN),
                STAT to null,
                POS to null)
        )

        val expectedIdList = Arrays.asList("0", "1", "2", "3", "4")

        assertThat(cfg)
                .haveBinding(Aes.MAP_ID, DATA_COLUMN_JOIN_KEY)
                .haveDataVectors(mapOf(
                        DATA_COLUMN_JOIN_KEY to expectedIdList,
                        NAME_COLUMN to GEO_DATA_FRAME_WITH_IDS[NAME_COLUMN] as List<*>))
                .haveMapIds(expectedIdList)
                .haveMapGeometries(GEO_DATA_FRAME_WITH_IDS[GEOMETRY_COLUMN] as List<*>)
    }

    @Test
    fun whenMapContainsGeoDataFrameWithID_shouldKeepIdAndGeometryColumns() {
        val cfg = createLayerConfigsByLayerSpec(geoPositionsDict(GEO_DATA_FRAME_WITH_IDS, GEO_DATA_FRAME_META))

        assertThat(cfg)
                .haveMapIds(GEO_DATA_FRAME_WITH_IDS[MAP_COLUMN_REQUEST] as List<*>)
                .haveMapGeometries(GEO_DATA_FRAME_WITH_IDS[GEOMETRY_COLUMN] as List<*>)
    }

    @Test
    fun whenMapContainsGeoDataFrameWithoutID_shouldKeepOnlyGeometryColumns() {
        val cfg = createLayerConfigsByLayerSpec(geoPositionsDict(GEO_DATA_FRAME_WITHOUT_IDS, GEO_DATA_FRAME_META))

        assertThat(cfg)
                .haveMapGeometries(GEO_DATA_FRAME_WITHOUT_IDS[GEOMETRY_COLUMN] as List<*>)
    }

    companion object {
        private const val NAME_COLUMN = "name"
        private const val VALUE_COLUMN = "value"
        private const val GEOMETRY_COLUMN = "coord"
        private val NAMES = listOf("foo", "bar", "qwe", "tmp", "xyz")
        private val VALUES = listOf("254", "313", "142", "89", "3")
        private val GEOMETRIES = listOf(
                "{'type: 'Point', 'coordinates':[-58, -34]}",
                "{'type: 'Point', 'coordinates':[-47, -15]}",
                "{'type: 'Point', 'coordinates':[-70, -33]}",
                "{'type: 'Point', 'coordinates':[-74, -11]}",
                "{'type: 'Point', 'coordinates':[-66, -17]}"
        )

        private val GEO_DATA_FRAME_WITH_IDS = mapOf(
                NAME_COLUMN to NAMES,
                VALUE_COLUMN to VALUES,
                MAP_COLUMN_REQUEST to listOf("rq_foo", "rq_bar", "rq_qwe", "rq_tmp", "rq_xyz"),
                MAP_COLUMN_REGION to listOf("rg_foo", "rg_bar", "rg_qwe", "rg_tmp", "rg_xyz"),
                MAP_COLUMN_JOIN_KEY to listOf("id_foo", "id_bar", "id_qwe", "id_tmp", "id_xyz"),
                GEOMETRY_COLUMN to GEOMETRIES
        )

        private val GEO_DATA_FRAME_WITHOUT_IDS = mapOf(
                NAME_COLUMN to NAMES,
                VALUE_COLUMN to VALUES,
                GEOMETRY_COLUMN to GEOMETRIES
        )

        private val GEO_DATA_FRAME_META = mapOf(
                GeoDataFrame.TAG to mapOf(
                        GeoDataFrame.GEOMETRY to GEOMETRY_COLUMN
                )
        )
    }
}