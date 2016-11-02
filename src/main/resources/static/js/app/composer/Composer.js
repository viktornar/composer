define([
    "dojo/Evented"
    , "dojo/_base/declare"
    , "dojo/_base/connect"
    , "dojo/_base/lang"
    , "dojo/_base/array"
    , "esri/request"
    , "dojo/dom"
    , "dojo/dom-construct"
    , "dojo/dom-class"
    , "esri/geometry/ScreenPoint"
    , "esri/geometry/Point"
    , "esri/geometry/Extent"
    , "esri/geometry/screenUtils"
    , "app/composer/Draggable"
], function (Evented
    , declare
    , connect
    , lang
    , array
    , request
    , dom
    , domConstruct
    , domClass
    , ScreenPoint
    , Point
    , Extent
    , screenUtils
    , Draggable) {
    return declare("app.composer.Composer", [Evented], {
        map: null,
        options: {
            pageHeight: 150,
            minHeight: 80,
            paddingToEdge: 30,
            keepAspectRatio: true
        },
        offset: {x: 0, y: 0},
        dimensions: {},
        scales: [
            1.0E7,
            5000000,
            3000000,
            2000000,
            1000000,
            500000,
            250000,
            100000,
            50000,
            25000,
            10000,
            5000,
            2000,
            1000
        ],
        refs: {
            paper_aspect_ratios: {
                letter: {landscape: 1.294, portrait: 0.773, scale: 1},
                a3: {landscape: 1.414, portrait: 0.707, scale: 1.414},
                a4: {landscape: 1.414, portrait: 0.707, scale: 1}
            },
            toolScale: 1,
            zoomScale: 1,
            startZoom: null,
            paperSize: 'letter',
            pageOrientation: 'landscape',
            page_aspect_ratio: null,
            page_dimensions: {
                width: 0,
                height: 0
            },
            rows: 1,
            cols: 2,
            prevRows: 1,
            prevCols: 2
        },
        constructor: function (config) {
            lang.mixin(this, config);
            this.refs.page_aspect_ratio = this.refs.paper_aspect_ratios[this.refs.paperSize][this.refs.pageOrientation];
            this._width = (this.options.pageHeight * this.refs.page_aspect_ratio) * this.refs.cols;
            this._height = this.options.pageHeight * this.refs.rows;

            // set inital zoom level
            this.refs.startZoom = this.map.getLevel();

            this._limitChangeFire = this.limitExecByInterval(function () {
                this.emit("change");
            }, 500, this);

            this._createElements();
            this._render();
        },
        remove: function () {
            this.map.on("pan-end", lang.hitch(this, this._onMapMovement));
            this.map.on("zoom-end", lang.hitch(this, this._onMapReset));
            this.map.on("reposition", lang.hitch(this, this._onMapMovement));
            this.map.on("resize", lang.hitch(this, this._onMapMovement));

            if (this._scaleConnect) connect.disconnect(this._scaleHandle);

            this._container.parentNode.removeChild(this._container);
        },
        setPaperSize: function (x) {
            if (x === this.refs.paperSize || !this.refs.paper_aspect_ratios[x]) return this;

            this.refs.paperSize = x;
            this.refs.page_aspect_ratio = this.refs.paper_aspect_ratios[this.refs.paperSize][this.refs.pageOrientation];

            this._updateScale();

            // if the new size is outside the map bounds, contain it.
            var mapBds = this.map.extent;
            if (!mapBds.contains(this.bounds)) {
                this.map.setExtent(this.bounds, true);
            }

            this.emit("change");
            return this;
        },
        setOrientation: function (x) {
            if (this.refs.paper_aspect_ratios[this.refs.paperSize][x] &&
                this.refs.page_aspect_ratio !== this.refs.paper_aspect_ratios[this.refs.paperSize][x]) {

                this.refs.pageOrientation = x;
                this.refs.page_aspect_ratio = this.refs.paper_aspect_ratios[this.refs.paperSize][x];

                this._updateAspectRatio();

                // if the flop is outside the map bounds, contain it.
                var mapBds = this.map.extent;

                if (!mapBds.contains(this.bounds)) {
                    this.map.setExtent(this.bounds, true);
                }

                this.emit("change");
            }

            return this;
        },
        _createElements: function () {
            if (!!this._container) return;

            this._container = domConstruct.create(
                "div",
                {
                    "class": "composer-areaselect-container"
                },
                dom.byId(this.map.id + "_layers"),
                "before"
            );

            this._grid = domConstruct.create(
                'div',
                {
                    'class': 'composer-areaselect-grid'
                },
                this._container
            );

            this._topShade = domConstruct.create(
                "div",
                {
                    'class': "composer-areaselect-shade"
                },
                this._container
            );

            this._bottomShade = domConstruct.create(
                "div",
                {
                    'class': "composer-areaselect-shade"
                },
                this._container
            );

            this._leftShade = domConstruct.create(
                "div",
                {
                    'class': "composer-areaselect-shade"
                },
                this._container
            );

            this._rightShade = domConstruct.create(
                "div",
                {
                    'class': "composer-areaselect-shade"
                },
                this._container
            );

            this._createPageModifiers();
            this._createContainerModifiers();
            this._calculateInitialPositions();
            this._setDimensions();
            this._createPages();

            this.map.on("pan-end", lang.hitch(this, this._onMapMovement));
            this.map.on("zoom-end", lang.hitch(this, this._onMapReset));
            this.map.on("reposition", lang.hitch(this, this._onMapMovement));
            this.map.on("resize", lang.hitch(this, this._onMapMovement));

            this.emit("change");
        },
        // Adds the add/remove buttons to the tool
        _createPageModifiers: function () {
            // row
            this._rowModifier = domConstruct.create(
                "div",
                {
                    'class': "composer-areaselect-handle page-tool row-modifier"
                },
                this._container
            );

            this._addRow = domConstruct.create(
                "div",
                {
                    'class': "modifier-btn add-btn"
                },
                this._rowModifier
            );

            this._createInnerText(this._addRow, "+");

            this._minusRow = domConstruct.create(
                "div",
                {
                    'class': "modifier-btn subtract-btn"
                },
                this._rowModifier
            );

            this._createInnerText(this._minusRow, "&#8722;");

            this._colModifier = domConstruct.create(
                "div",
                {
                    'class': "composer-areaselect-handle page-tool col-modifier"
                },
                this._container
            );

            this._addCol = domConstruct.create("div", {
                'class': "modifier-btn add-btn"
            }, this._colModifier);

            this._createInnerText(this._addCol, "+");

            this._minusCol = domConstruct.create("div", {
                'class': "modifier-btn subtract-btn"
            }, this._colModifier);

            this._createInnerText(this._minusCol, "&#8722;");

            connect.connect(this._addRow, "click", lang.hitch(this, this._onAddRow));
            connect.connect(this._minusRow, "click", lang.hitch(this, this._onSubtractRow));
            connect.connect(this._addCol, "click", lang.hitch(this, this._onAddCol));
            connect.connect(this._minusCol, "click", lang.hitch(this, this._onSubtractCol));
        },
        _createInnerText: function (container, text) {
            domConstruct.create(
                "div",
                {
                    'innerHTML': text
                },
                container
            );
        },
        // Adds the scale & drag buttons
        _createContainerModifiers: function () {
            // scale button
            this._setScaleHandler(domConstruct.create(
                "div",
                {
                    "class": "composer-areaselect-handle scale-handle"
                },
                this._container),
                -1,
                -1
            );

            // drag button
            this._dragHandle = domConstruct.create(
                "div",
                {
                    "class": "composer-areaselect-handle drag-handle"
                },
                this._container
            );

            var draggable = new Draggable({
                element: this._dragHandle,
                dragStartTarget: null,
                getPosition: this._getPos,
                setPosition: this._setPos,
                context: this
            });
            draggable.enable();
        },
        _onAddRow: function (evt) {
            evt.stopPropagation();
            this.refs.rows++;
            this._updatePages();
        },
        _onSubtractRow: function (evt) {
            evt.stopPropagation();
            if (this.refs.rows === 1) return;
            this.refs.rows--;
            this._updatePages();
        },
        _onAddCol: function (evt) {
            evt.stopPropagation();
            this.refs.cols++;
            this._updatePages();
        },
        _onSubtractCol: function (evt) {
            evt.stopPropagation();
            if (this.refs.cols === 1) return;
            this.refs.cols--;
            this._updatePages();
        },
        // Handler for when the tool is scaled
        _setScaleHandler: function (handle, xMod, yMod) {
            if (this._scaleHandle) return;

            xMod = xMod || 1;
            yMod = yMod || 1;

            this._scaleHandle = handle;

            this._scaleProps = {
                x: xMod,
                y: yMod,
                curX: 0,
                curY: 0,
                maxHeight: 0,
                ratio: 1
            };

            this._scaleConnect = connect.connect(this._scaleHandle, "mousedown", lang.hitch(this, this._onScaleMouseDown));
        },
        _onScaleMouseDown: function (event) {
            event.stopPropagation();
            connect.disconnect(this._scaleConnect);

            this._scaleProps.curX = event.pageX;
            this._scaleProps.curY = event.pageY;
            this._scaleProps.ratio = this.dimensions.width / this.dimensions.height;

            domClass.add(this._container, 'scaling');

            var extent = this.bounds;
            var width = this.map.width;
            var height = this.map.height;

            var nw = new Point(extent.xmin, extent.ymax, this.map.spatialReference);
            var nwPt = screenUtils.toScreenPoint(this.map.extent, width, height, nw);

            var maxHeightY = height - nwPt.y - this.options.paddingToEdge;
            var maxHeightX = (width - nwPt.x - this.options.paddingToEdge) / this._scaleProps.ratio;

            this._scaleProps.maxHeight = Math.min(maxHeightY, maxHeightX);

            this._mouseMoveConnect = this.map.on("mouse-move", lang.hitch(this, this._onScaleMouseMove));
            this._mouseUpConnect = this.map.on("mouse-up", lang.hitch(this, this._onScaleMouseUp));
        },
        _onScaleMouseMove: function (event) {
            var width = this.dimensions.width,
                height = this.dimensions.height;

            var size = {x: this.map.width, y: this.map.height};

            if (this.options.keepAspectRatio) {
                // var maxHeight = (height >= width ? size.y : size.y * (1/this._scaleProps.ratio) ) - 30;
                this._scaleProps.sign = this._scaleProps.curY - event.pageY;

                height += (this._scaleProps.curY - event.pageY) * 2 * this._scaleProps.y;
                height = Math.max(this.options.minHeight, height);
                height = Math.min(this._scaleProps.maxHeight, height);
                width = height * this._scaleProps.ratio;

            } else {
                this._width += (this._scaleProps.curX - event.pageX) * 2 * this._scaleProps.x;
                this._height += (this._scaleProps.curY - event.pageY) * 2 * this._scaleProps.y;
                this._width = Math.max(this.options.paddingToEdge, this._width);
                this._height = Math.max(this.options.paddingToEdge, this._height);
                this._width = Math.min(size.x - this.options.paddingToEdge, this._width);
                this._height = Math.min(size.y - this.options.paddingToEdge, this._height);
            }

            this.dimensions.width = width;
            this.dimensions.height = height;

            this._scaleProps.curX = event.pageX;
            this._scaleProps.curY = event.pageY;

            this.bounds = this._getBoundsPinToNorthWest();
            this._render();
        },
        // TODO: check if i still need it
        _continueScale: function () {
            var width = this.dimensions.width,
                height = this.dimensions.height;

            var spatialReference = this.map.spatialReference;
            var scale = esri.geometry.getScale(this.bounds, 800, spatialReference);
            var closestScale = this._getClosestScale(Math.round(scale));

            var setDimensions = function (incrementBy, context) {
                height = context.dimensions.height + incrementBy;
                width = height * context._scaleProps.ratio;
                context.dimensions.width = width;
                context.dimensions.height = height;
                context.bounds = context._getBoundsPinToNorthWest();
                context._setDimensions();
            };

            if (Math.round(scale) - closestScale > 0) {
                while (scale - closestScale > 0) {
                    setDimensions(-1, this);
                }
            } else {
                while (scale - closestScale < 0) {
                    setDimensions(+1, this);
                }
            }

            this._render();
        },
        _getClosestScale: function (curScale) {
            var scales = this.scales;

            return scales.reduce(function (prev, curr) {
                return (Math.abs(curr - curScale) < Math.abs(prev - curScale) ? curr : prev);
            });
        },
        _onScaleMouseUp: function (event) {
            this._mouseMoveConnect.remove();
            this._mouseUpConnect.remove();
            this._scaleConnect = dojo.connect(this._scaleHandle, "mousedown", lang.hitch(this, this._onScaleMouseDown));
            domClass.remove(this._container, 'scaling');
            this.emit("change");
        },
        _calculateInitialPositions: function () {
            var size = {x: this.map.width, y: this.map.height};

            var topBottomHeight = Math.round((size.y - this._height) / 2);
            var leftRightWidth = Math.round((size.x - this._width) / 2);
            this.nwPosition = {x: leftRightWidth + this.offset.x, y: topBottomHeight + this.offset.y};
            this.nwLocation = screenUtils.toMapPoint(
                this.map.extent,
                size.x,
                size.y,
                new ScreenPoint(this.nwPosition)
            );
            this.bounds = this.getBounds();
        },
        _setDimensions: function () {
            var extent = this.bounds;
            var width = this.map.width;
            var height = this.map.height;

            var nw = new Point(extent.xmin, extent.ymax, this.map.spatialReference);
            var sw = new Point(extent.xmin, extent.ymin, this.map.spatialReference);
            var ne = new Point(extent.xmax, extent.ymax, this.map.spatialReference);
            var se = new Point(extent.xmax, extent.ymin, this.map.spatialReference);

            this.dimensions.nw = screenUtils.toScreenPoint(this.map.extent, width, height, nw);
            this.dimensions.ne = screenUtils.toScreenPoint(this.map.extent, width, height, ne);
            this.dimensions.sw = screenUtils.toScreenPoint(this.map.extent, width, height, sw);
            this.dimensions.se = screenUtils.toScreenPoint(this.map.extent, width, height, se);

            this.dimensions.width = this.dimensions.ne.x - this.dimensions.nw.x;
            this.dimensions.height = this.dimensions.se.y - this.dimensions.ne.y;

            this.dimensions.cellWidth = this.dimensions.width / this.refs.cols;
            this.dimensions.cellHeight = this.dimensions.height / this.refs.rows;
        },
        _createPages: function () {
            var cols = this.refs.cols,
                rows = this.refs.rows,
                gridElm = this._grid,
                top = this.dimensions.nw.y,
                left = this.dimensions.nw.x,
                width = this.dimensions.width,
                height = this.dimensions.height;

            domClass.remove(this._container, "one-row");
            domClass.remove(this._container, "one-col");

            if (cols === 1) domClass.add(this._container, "one-col");
            if (rows === 1) domClass.add(this._container, "one-row");

            this._grid.innerHTML = "";
            this._grid.style.top = top + "px";
            this._grid.style.left = left + "px";
            this._grid.style.width = width + "px";
            this._grid.style.height = height + "px";

            var spacingX = 100 / cols,
                spacingY = 100 / rows;

            // cols
            for (var i = 0; i < cols; i++) {
                for (var r = 0; r < rows; r++) {
                    var elm = gridElm.appendChild(this._makePageElement(spacingX * i, spacingY * r, spacingX, spacingY));

                    // adjust borders
                    if (r === 0) {
                        domClass.add(elm, 'outer-top');
                    } else {
                        domClass.add(elm, 'no-top');
                    }

                    if (r == rows - 1) {
                        domClass.add(elm, 'outer-bottom');
                    }

                    if (i === 0) {
                        domClass.add(elm, 'outer-left');
                    } else {
                        domClass.add(elm, 'no-left');
                    }
                    if (i == cols - 1) {
                        domClass.add(elm, 'outer-right');
                    }
                }
            }
        },
        _makePageElement: function (x, y, w, h) {
            var div = document.createElement('div');
            div.className = "page";
            div.style.left = x + "%";
            div.style.top = y + "%";
            div.style.height = h + "%";
            div.style.width = w + "%";
            return div;
        },
        _render: function () {
            var size = {x: this.map.width, y: this.map.height};

            if (!this.nwPosition) {
                this._calculateInitialPositions();
            }

            this._setDimensions();

            var nw = this.dimensions.nw,
                ne = this.dimensions.ne,
                sw = this.dimensions.sw,
                se = this.dimensions.se,
                width = this.dimensions.width,
                height = this.dimensions.height,
                rightWidth = size.x - width - nw.x,
                bottomHeight = size.y - height - nw.y;

            // position page grid
            this._updatePageGridPosition(nw.x, nw.y, width, height);

            // position shades
            this._updateGridElement(this._topShade, {
                width: size.x,
                height: nw.y > 0 ? nw.y : 0,
                top: 0,
                left: 0
            });

            this._updateGridElement(this._bottomShade, {
                width: size.x,
                height: bottomHeight > 0 ? bottomHeight : 0,
                bottom: 0,
                left: 0
            });

            this._updateGridElement(this._leftShade, {
                width: nw.x > 0 ? nw.x : 0,
                height: height,
                top: nw.y,
                left: 0
            });

            this._updateGridElement(this._rightShade, {
                width: rightWidth > 0 ? rightWidth : 0,
                height: height,
                top: nw.y,
                right: 0
            });

            // position handles
            this._updateGridElement(this._dragHandle, {left: nw.x, top: nw.y});
            this._updateGridElement(this._scaleHandle, {left: nw.x + width, top: nw.y + height});

            this._updateGridElement(this._rowModifier, {left: nw.x + (width / 2), top: nw.y + height});
            this._updateGridElement(this._colModifier, {left: nw.x + width, top: nw.y + (height / 2)});
        },
        _updateGridElement: function (element, dimension) {
            element.style.width = dimension.width + "px";
            element.style.height = dimension.height + "px";
            element.style.top = dimension.top + "px";
            element.style.left = dimension.left + "px";
            element.style.bottom = dimension.bottom + "px";
            element.style.right = dimension.right + "px";
        },
        _updatePageGridPosition: function (left, top, width, height) {
            this._grid.style.top = top + "px";
            this._grid.style.left = left + "px";
            this._grid.style.width = width + "px";
            this._grid.style.height = height + "px";
        },
        _updateAspectRatio: function () {
            //switch from landscape to portrait
            this.dimensions.height = this.dimensions.cellWidth * this.refs.rows;
            this.dimensions.width = this.dimensions.cellHeight * this.refs.cols;

            // re-calc bounds
            this.bounds = this._getBoundsPinToNorthWest();
            this._render();
        },
        _updateScale: function () {
            //switch between letter/a3/a4
            var scale = this.refs.paper_aspect_ratios[this.refs.paperSize].scale;

            if (scale > this.refs.toolScale) {
                this.dimensions.width = this.dimensions.width * scale;
                this.refs.toolScale = scale;
            } else if (scale < this.refs.toolScale) {
                this.dimensions.width = this.dimensions.width / this.refs.toolScale;
                this.refs.toolScale = scale;
            }

            this.dimensions.height = ((this.dimensions.width / this.refs.cols) / this.refs.page_aspect_ratio) * this.refs.rows;

            // re-calc bounds
            this.bounds = this._getBoundsPinToNorthWest();
            this._render();
        },
        _updateToolDimensions: function () {
            if (this.refs.cols !== this.refs.prevCols) {
                var width = this.dimensions.width / this.refs.prevCols;
                this.dimensions.width = width * this.refs.cols;
                this.refs.prevCols = this.refs.cols;
            }

            if (this.refs.rows !== this.refs.prevRows) {
                var height = this.dimensions.height / this.refs.prevRows;
                this.dimensions.height = height * this.refs.rows;
                this.refs.prevRows = this.refs.rows;
            }

            // re-calc bounds
            this.bounds = this._getBoundsPinToNorthWest();
            this._render();
        },
        _onMapMovement: function () {
            this._render();
            this.emit("change");
        },
        _onMapReset: function () {
            this.refs.zoomScale = 1 / this.map.getLevel();
            this._render();
            this.emit("change");
        },
        _getPos: function (ctx) {
            var extent = this.map.extent,
                width = this.map.width,
                height = this.map.height;

            return screenUtils.toScreenPoint(extent, width, height, this.nwLocation);
        },
        _setPos: function (pos, delta) {
            this._updateNWPosition(pos);
            this._render();
            this._limitChangeFire();
        },
        _updateNWPosition: function (pos) {
            var extent = this.map.extent,
                width = this.map.width,
                height = this.map.height;

            this.nwPosition = pos;

            this.nwLocation = screenUtils.toMapPoint(
                extent,
                width,
                height,
                new ScreenPoint(this.nwPosition)
            );

            this.bounds = this._getBoundsPinToNorthWest();
        },
        _updatePages: function () {
            this._setDimensions();
            this._updateToolDimensions();
            this._createPages();

            this.emit("change");
        },
        getPages: function () {
            return {cols: this.refs.cols, rows: this.refs.rows};
        },
        getPinnedBounds: function () {
            return this.bounds || null;
        },
        getBounds: function () {
            var size = {x: this.map.width, y: this.map.height};
            var topRight = {x: 0, y: 0};
            var bottomLeft = {x: 0, y: 0};

            bottomLeft.x = Math.round((size.x - this._width) / 2);
            topRight.y = Math.round((size.y - this._height) / 2);
            topRight.x = size.x - bottomLeft.x;
            bottomLeft.y = size.y - topRight.y;

            var sw = screenUtils.toMapPoint(this.map.extent, size.x, size.y, new ScreenPoint(bottomLeft));
            var ne = screenUtils.toMapPoint(this.map.extent, size.x, size.y, new ScreenPoint(topRight));

            return new Extent(sw.x, sw.y, ne.x, ne.y, this.map.spatialReference);
        },
        _getBoundsPinToNorthWest: function () {
            var width = this.map.width;
            var height = this.map.height;

            var topRight = {x: 0, y: 0};
            var bottomLeft = {x: 0, y: 0};

            var nwPoint = screenUtils.toScreenPoint(this.map.extent, width, height, this.nwLocation);

            topRight.y = nwPoint.y;
            bottomLeft.y = nwPoint.y + this.dimensions.height;
            bottomLeft.x = nwPoint.x;
            topRight.x = nwPoint.x + this.dimensions.width;

            var sw = screenUtils.toMapPoint(this.map.extent, width, height, new ScreenPoint(bottomLeft));
            var ne = screenUtils.toMapPoint(this.map.extent, width, height, new ScreenPoint(topRight));

            return new Extent(sw.x, sw.y, ne.x, ne.y, this.map.spatialReference);
        },
        // TODO: explain for yourself why you need timeout function with lock?
        limitExecByInterval: function (fn, time, context) {
            var lock, execOnUnlock;

            return function wrapperFn() {
                var args = arguments;

                if (lock) {
                    execOnUnlock = true;
                    return;
                }

                lock = true;

                setTimeout(function () {
                    lock = false;

                    if (execOnUnlock) {
                        wrapperFn.apply(context, args);
                        execOnUnlock = false;
                    }
                }, time);

                fn.apply(context, args);
            };
        }
    });
});  
