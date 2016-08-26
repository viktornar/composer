define([
    "app/composer/Composer"
    , "esri/map"
    , "dojo/on"
    , "dojo/dom"
    , "esri/layers/ArcGISTiledMapServiceLayer"
    , "esri/geometry/Extent"
    , "esri/geometry/screenUtils"
    , "dojo/domReady!"
], function (Composer
    , Map
    , on
    , dom
    , Tiled
    , Extent
    , screenUtils) {

    var URL = {
        BACKROUND_LAYER: "http://www.geoportal.lt/arcgis/rest/services/geoportal_public/background_Lietuva-3346/MapServer"
    };

    // cache some elements
    var atlasRows = dom.byId('rows'),
        atlasCols = dom.byId('columns'),
        atlasPaperSize = dom.byId('size'),
        atlasOrientation = dom.byId('orientation'),
        extentXmin = dom.byId('extent.xmin'),
        extentYmin = dom.byId('extent.ymin'),
        extentXmax = dom.byId('extent.xmax'),
        extentYmax = dom.byId('extent.ymax');

    var map = new Map("map", {
        autoResize: true,
        extent: new Extent({
            xmin: 579929.8205909743,
            ymin: 6057078.466387265,
            xmax: 592629.8459910251,
            ymax: 6066868.069299805,
            spatialReference: {
                wkid: 3346
            }
        })
    });

    var tiled = new Tiled(URL.BACKROUND_LAYER);

    map.addLayer(tiled);

    function setPartialFormValues(composer) {
        var pages = composer.getPages();
        var bds = composer.getPinnedBounds();
        atlasRows.value = pages.rows;
        atlasCols.value = pages.cols;

        extentXmin.value = bds.xmin;
        extentYmin.value = bds.ymin;
        extentXmax.value = bds.xmax;
        extentYmax.value = bds.ymax;
    }

    map.on("load", function () {
        var composer = new Composer({map: map, width: 200, height: 300});
        composer.on('change', function () {
            setPartialFormValues(composer);
        });

        setPartialFormValues(composer);

        composer.setPaperSize(atlasPaperSize.value);
        composer.setOrientation(atlasOrientation.value);

        on(atlasPaperSize, 'change', function () {
            composer.setPaperSize(this.value);
        });

        on(atlasOrientation, 'change', function () {
            composer.setOrientation(this.value);
        });
    });
});
