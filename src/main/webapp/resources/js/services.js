'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('dashboard.services', ['ngResource'])
    .value('version', '0.1')
    .factory('Campaigns', function ($resource) {
        return $resource('campaigns/:campaignId.json', {}, {})
    });
