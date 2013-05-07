'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('dashboard.services', ['ngResource'])
    .value('version', '0.1')
    .factory('Campaign', function ($resource) {
        return $resource('/api/campaigns/:campaignId', {}, {})
    })
    .factory('Geofence', function ($resource) {
        return $resource('/api/geofencelist/:id', {}, {})
    })
    .factory('GeofenceList', function ($resource) {
        return $resource('/api/geofencelists/:appId', {}, {})
    })
    .factory('Factual', function ($resource) {
        return $resource('/api/factual', {}, {})
    })
