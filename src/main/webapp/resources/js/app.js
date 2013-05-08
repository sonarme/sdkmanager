'use strict';


// Declare app level module which depends on filters, and services
angular.module('dashboard', ['ui', 'ui.bootstrap', '$strap.directives', 'dashboard.filters', 'dashboard.services', 'dashboard.directives', 'dashboard.controllers']).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/analytics', {templateUrl: 'partials/analytics.html', controller: 'Analytics'})
            .when('/campaigns', {templateUrl: 'partials/campaigns.html', controller: 'CampaignsCtrl'})
            .when('/marketingBuild', {templateUrl: 'partials/marketingBuild.html', controller: 'MarketingBuild'})
            .when('/geofenceBuild', {templateUrl: 'partials/geofenceBuild.html', controller: 'GeofenceBuildCtrl'})
            .when('/geofenceLists', {templateUrl: 'partials/geofenceLists.html', controller: 'GeofenceListsCtrl'})
            .otherwise({redirectTo: '/analytics'});
    }]);
