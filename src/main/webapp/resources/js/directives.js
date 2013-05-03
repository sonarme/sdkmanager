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
        }])

    .directive('dateAfter', function () {
        return {
            // restrict to an attribute type.
            restrict: 'A',
            require: 'ngModel',
            // scope = the parent scope
            // elem = the element the directive is on
            // attr = a dictionary of attributes on the element
            // ctrl = the controller for ngModel.
            link: function ($scope, elem, attr, ctrl) {
                alert(JSON.stringify($scope.startDate, undefined, 2));
                alert(JSON.stringify($scope.endDate, undefined, 2));
                // add a parser that will process each time the value is
                // parsed into the model when the user updates it.
                ctrl.$parsers.unshift(function (value) {

                    var beforeDate = $scope[attr.dateAfter];
                    alert(beforeDate);
                    var afterDate = $scope[attr.ngModel];
                    alert(afterDate);
                    ctrl.$setValidity('dateAfter', true);

                    // if it's valid, return the value to the model,
                    // otherwise return undefined.
                    return true ? value : undefined;
                });

                // add a formatter that will process each time the value
                // is updated on the DOM element.
                ctrl.$formatters.unshift(function (value) {
                    alert($scope);
                    var beforeDate = $scope[attr.dateAfter];
                    alert(beforeDate);
                    var afterDate = $scope[attr.ngModel];
                    alert(afterDate);
                    // validate.
                    ctrl.$setValidity('dateAfter', true);

                    // return the value or nothing will be written to the DOM.
                    return value;
                });
            }
        };
    });

