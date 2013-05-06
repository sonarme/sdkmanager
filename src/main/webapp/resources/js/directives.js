'use strict';

/* Directives */


angular.module('dashboard.directives', [])
    .directive('appVersion', ['version', function (version) {
        return function (scope, elm, attrs) {
            elm.text(version);
        };
    }])

    .directive('dropdownToggleKeep',
        ['$document', '$location', '$window', function ($document, $location, $window) {
            var openElement = null,
                closeMenu = angular.noop;
            return {
                restrict: 'CA',
                link: function (scope, element, attrs) {
                    scope.$watch('$location.path', function () {
                        closeMenu();
                    });
                    element.parent().bind('click', function () {
                        //closeMenu();
                    });
                    element.bind('click', function (event) {
                        event.preventDefault();
                        event.stopPropagation();
                        var elementWasOpen = (element === openElement);
                        if (!!openElement) {
                            closeMenu();
                        }
                        if (!elementWasOpen) {
                            element.parent().addClass('open');
                            openElement = element;
                            closeMenu = function (event) {
                                if (event) {
                                    event.preventDefault();
                                    event.stopPropagation();
                                }
                                //$document.unbind('click', closeMenu);
                                element.parent().removeClass('open');
                                closeMenu = angular.noop;
                                openElement = null;
                            };
                            //$document.bind('click', closeMenu);
                        }
                    });
                }
            };
        }]);


