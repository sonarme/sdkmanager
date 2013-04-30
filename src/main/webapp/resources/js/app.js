'use strict';


// Declare app level module which depends on filters, and services
angular.module('dashboard', ['dashboard.filters', 'dashboard.services', 'dashboard.directives', 'dashboard.controllers']).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/marketingBuild', {templateUrl: 'partials/marketingBuild.html', controller: 'MarketingBuild'});
        $routeProvider.otherwise({redirectTo: '/marketingBuild'});
    }]);
