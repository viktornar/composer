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
    , "dojo/dom-style"
    , "esri/geometry/ScreenPoint"
    , "esri/geometry/Point"
    , "esri/geometry/Extent"
    , "esri/geometry/screenUtils"
], function (Evented
    , declare
    , connect
    , lang
    , array
    , request
    , dom
    , domConstruct
    , domClass
    , domStyle
    , ScreenPoint
    , Point
    , Extent
    , screenUtils) {
    return declare("app.composer.Draggable", [Evented], {
        getPosition: function () {
        },
        setPosition: function () {
        },

        _element: null,
        _dragStartTarget: null,
        _context: null,
        _connects: [],
        _moveConnects: [],
        _lastTime: 0,

        START: ['mousedown'],
        END: {
            mousedown: 'mouseup',
            touchstart: 'touchend',
            pointerdown: 'touchend',
            MSPointerDown: 'touchend'
        },
        MOVE: {
            mousedown: 'mousemove',
            touchstart: 'touchmove',
            pointerdown: 'touchmove',
            MSPointerDown: 'touchmove'
        },

        constructor: function (config) {
            this.getPosition = config.getPosition;
            this.setPosition = config.setPosition;
            this._element = config.element;
            this._dragStartTarget = config.dragStartTarget || config.element;
            this._context = config.context;

            function getPrefixed(name) {
                return window['webkit' + name] || window['moz' + name] || window['ms' + name];
            }

            this.requestFn = window.requestAnimationFrame ||
                getPrefixed('RequestAnimationFrame') ||
                this.timeoutDefer;

            this.cancelFn = window.cancelAnimationFrame ||
                getPrefixed('CancelAnimationFrame') ||
                getPrefixed('CancelRequestAnimationFrame') ||
                function (id) {
                    window.clearTimeout(id);
                };
        },

        enable: function () {
            if (this._enabled) {
                return;
            }

            array.forEach(this.START, function (evt) {
                this._connects.push(connect.connect(this._dragStartTarget, evt, lang.hitch(this, this._onDown)));
            }, this);

            this._enabled = true;
        },

        disable: function () {
            if (!this._enabled) return;


            array.forEach(this._connects, connect.disconnect);

            this._enabled = false;
            this._moved = false;
        },

        _onDown: function (evt) {
            this._moved = false;

            if (evt.shiftKey || ((evt.which !== 1) && (evt.button !== 1) && !evt.touches)) {
                return;
            }

            evt.stopPropagation();

            // disable map dragging while dragging tool.
            try {
                this._context.map.disablePan();
            } catch (evt) {
                console.error("error on evt: ", evt)
            }

            if (domClass.contains(this._element, 'composer-zoom-anim')) {
                return;
            }

            if (this._moving) return;


            this.emit('down');

            var first = evt.touches ? evt.touches[0] : evt;

            this._startPoint = {
                x: first.clientX,
                y: first.clientY
            };

            this._startPos = this._newPos = this.getPosition.call(this._context);

            this._moveConnects.push(
                connect.connect(
                    document,
                    this.MOVE[evt.type],
                    lang.hitch(this, this._onMove)
                )
            );

            this._moveConnects.push(
                connect.connect(
                    document,
                    this.END[evt.type],
                    lang.hitch(this, this._onUp)
                )
            );

            evt.preventDefault();
        },

        _onMove: function (e) {
            if (e.touches && e.touches.length > 1) {
                this._moved = true;
                return;
            }

            var first = (e.touches && e.touches.length === 1 ? e.touches[0] : e),
                newPoint = {x: first.clientX, y: first.clientY};

            var offset = this._substract(newPoint, this._startPoint);
            if (!offset.x && !offset.y) return;

            e.preventDefault();

            if (!this._moved) {
                this.emit('dragstart');
                this._moved = true;
                this._startPos = this._substract(this.getPosition.call(this._context), offset);
                domClass.add(document.body, 'composer-dragging');
                this._lastTarget = e.target || e.srcElement;
                domClass.add(this._lastTarget, 'composer-drag-target');
            }

            this._newPos = this._add(this._startPos, offset);

            if (this._prevPos) {
                this._prevPos = this._substract(this._newPos, this._prevPos);
            } else {
                this._prevPos = offset;
            }

            this._prevPos = lang.clone(this._newPos);
            this._moving = true;
            this.cancelAnimFrame(this._animRequest);
            this._lastEvent = e;
            this._animRequest = this.requestAnimFrame(this._updatePosition, this, true, this._dragStartTarget);
        },
        _updatePosition: function () {
            var evt = {originalEvent: this._lastEvent};
            this.emit('predrag', evt);
            this.setPosition.call(this._context, this._newPos, this._offset);
            this.emit('drag', evt);
        },
        _onUp: function () {
            domClass.remove(document.body, 'composer-dragging');

            if (this._lastTarget) {
                domClass.remove(this._lastTarget, 'composer-drag-target');
                this._lastTarget = null;
            }

            array.forEach(this._moveConnects, connect.disconnect);

            if (this._moved && this._moving) {
                // ensure drag is not fired after dragend
                this.cancelAnimFrame(this._animRequest);

                this.emit('dragend', {
                    distance: this._distanceTo(this._newPos, this._startPos)
                });
            }

            // re-enable map dragging
            if (!this._context.map.isPan) {
                try {
                    this._context.map.enablePan();
                } catch (e) {
                    console.error("error on pan: ", e);
                }
            }

            this._moving = false;
        },
        _substract: function (point1, point2) {
            point = lang.clone(point1);
            point.x -= point2.x;
            point.y -= point2.y;
            return point;
        },
        _add: function (point1, point2) {
            point = lang.clone(point1);
            point.x += point2.x;
            point.y += point2.y;
            return point;
        },
        _distanceTo: function (point1, point2) {
            var x = point2.x - point1.x,
                y = point2.y - point1.y;

            return Math.sqrt(x * x + y * y);
        },
        requestAnimFrame: function (fn, context, immediate) {
            if (immediate && this.requestFn === this.timeoutDefer) {
                fn.call(context);
            } else {
                return this.requestFn.call(window, lang.hitch(context, fn));
            }
        },
        cancelAnimFrame: function (id) {
            if (id) {
                this.cancelFn.call(window, id);
            }
        },
        // fallback for IE 7-8
        timeoutDefer: function (fn) {
            var time = +new Date(),
                timeToCall = Math.max(0, 16 - (time - this._lastTime));

            this._lastTime = time + timeToCall;

            return window.setTimeout(fn, timeToCall);
        }
    });
});