'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('dashboard.services', ['ngResource'])
    .value('version', '0.1')
    .factory('Campaign', function ($resource) {
        return $resource('/api/campaigns/:campaignId', {}, {})
    })
    .factory('Factual', function ($resource) {
        return $resource('stubs/factual/:search.json', {}, {})
    })
