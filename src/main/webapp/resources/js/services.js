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
        return  {
            query: function (queryParams, successHandler, errorHandler) {
                successHandler([
                    {
                        id: "gf1",
                        name: "McDonalds"
                    },
                    {
                        id: "gf2",
                        name: "Burger King"
                    }
                ]);
            }
        }
    })
    .factory('Factual', function ($resource) {
        return $resource('/api/factual', {}, {})
    })
