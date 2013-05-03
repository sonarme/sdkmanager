'use strict';

/* Controllers */

angular.module('dashboard.controllers', [])
    .controller('MarketingBuild', ['$scope', 'Campaign', function ($scope, Campaign) {
        $scope.predicate = 'and';
        $scope.attributes = [

            {name: 'Gender', id: 'gender', group: 'Attribute', type: 'c', options: ['male', 'female']},
            {name: 'Income', id: 'income', group: 'Attribute', type: 'c', options: ['50k', '100k']},
            {name: 'Visits', id: 'visits', group: 'Visits', type: 'n'}

        ];
        $scope.dows = [
            {name: 'Monday', id: 2},
            {name: 'Tuesday', id: 3},
            {name: 'Wednesday', id: 4},
            {name: 'Thursday', id: 5},
            {name: 'Friday', id: 6},
            {name: 'Saturday', id: 7},
            {name: 'Sunday', id: 1}
        ];
        var hourMs = 60 * 60 * 1000
        $scope.frequencyCaps = [
            {name: 'per hour', id: hourMs},
            {name: 'per day', id: 24 * hourMs},
            {name: 'per week', id: 7 * 24 * hourMs},
            {name: 'per month', id: 30 * 7 * 24 * hourMs},
            {name: 'ever', id: -1}
        ];
        $scope.frequencyCap = $scope.frequencyCaps[$scope.frequencyCaps.length - 1];
        $scope.frequencyCapEnabled = true;
        $scope.compareOps = {
            'c': [
                {name: 'equals', id: 'EQUAL'},
                {name: 'does not equal', id: 'NOTEQUAL'}
            ],
            'n': [
                {name: 'equals', id: 'EQUAL'},
                {name: 'does not equal', id: 'NOTEQUAL'},
                {name: 'less than', id: 'LESS'},
                {name: 'less than or equal', id: 'LESSOREQUAL'},
                {name: 'greater than', id: 'GREATER'},
                {name: 'greater than or equal', id: 'GREATEROREQUAL'}
            ]
        }
        $scope.geofenceActions = [
            {name: "Arriving at", id: true},
            {name: "Leaving", id: false}
        ]
        $scope.geofenceLists = [
            {name: "Bla", id: "bla"}
        ]
        $scope.clauses = [

        ];
        $scope.geofenceEntries = [
        ];
        $scope.dowSelected = {
        };
        function _fn_error(err) {
            alert(err);

        }

        function _fn_success_put_post(data) {
        }

        $scope.setPredicate = function (pred) {
            $scope.predicate = pred;
        };
        $scope.save = function () {
            var campaign = new Campaign($scope.campaign);
            campaign.clauses = $scope.clauses;
            campaign.$save();
        };
        $scope.removeClause = function (clause) {
            $scope.clauses.splice($scope.clauses.indexOf(clause), 1);
        }
        $scope.createClause = function () {
            $scope.clauses.push({});
        }
        $scope.removeGeofenceEntry = function (ga) {
            $scope.geofenceEntries.splice($scope.geofenceEntries.indexOf(ga), 1);
        }
        $scope.addGeofenceEntry = function () {
            $scope.geofenceEntries.push({});
        }
        $scope.refreshDowAll = function () {
            for (var i = 1; i <= 7; ++i)
                $scope.dowSelected[i] = $scope.dowAll;
        }
        $scope.allDowSelected = function () {
            for (var i = 1; i <= 7; ++i)
                if (!$scope.dowSelected[i]) return false;
            return true;
        }

    }])
    .controller('GeofenceBuildCtrl', ['$scope', 'Factual', function ($scope, Factual) {
        $scope.search = function () {
            this.limit = 50;
            $scope.places = Factual.get(this);
        }
        $scope.removePlace = function () {

        }
        $scope.showResultsTable = function () {
            return $scope.places !== undefined;
        }
    }]);