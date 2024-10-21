terraform {
  required_providers {
    permitio = {
      source  = "permitio/permit-io"
      version = "~> 0.0.1"
    }
  }
}

variable "permit_api_key" {
  type        = string
  description = "permit_key_Jo2McGAX9UqP5FCoqegTsxxePGQIR3l0QAukq1JptVj1GqO9IvvE7U62FghO49pMVtNyv22OvDQtv321tJDwEd"
}

provider "permitio" {
  api_url = "https://api.permit.io"
  api_key = var.permit_api_key
}

resource "permitio_resource" "product" {
  key     = "product"
  name    = "Products"
  actions = {
    "create" = { "name" = "create" }
    "read"   = { "name" = "read" }
    "update" = { "name" = "update" }
    "delete" = { "name" = "delete" }
  }
  attributes = {
    "vendor" = {
      "description" = "The user key who created the product"
      "type"        = "string"
    }
  }
}

resource "permitio_resource_set" "own_product" {
  key        = "own_product"
  name       = "Own Products"
  resource   = permitio_resource.product.key
  conditions = jsonencode({
    "allOf" : [
      {
        "allOf" : [
          { "resource.vendor" : { "equals" : { "ref" : "user.key" } } },
        ],
      },
    ],
  })
  depends_on = [
    permitio_resource.product,
  ]
}

resource "permitio_resource" "review" {
  key     = "review"
  name    = "Reviews"
  actions = {
    "create" = { "name" = "create" }
    "read"   = { "name" = "read" }
    "update" = { "name" = "update" }
    "delete" = { "name" = "delete" }
  }
  attributes = {
    "customer" = {
      "description" = "The user key who created the review"
      "type"        = "string"
    }
  }
}

resource "permitio_resource_set" "own_review" {
  key        = "own_review"
  name       = "Own Reviews"
  resource   = permitio_resource.review.key
  conditions = jsonencode({
    "allOf" : [
      {
        "allOf" : [
          { "resource.customer" : { "equals" : { "ref" : "user.key" } } },
        ],
      },
    ],
  })
  depends_on = [
    permitio_resource.review,
  ]
}

resource "permitio_relation" "product_review_relation" {
  key              = "parent"
  name             = "Product parent of Review"
  subject_resource = permitio_resource.product.key
  object_resource  = permitio_resource.review.key
  depends_on       = [
    permitio_resource.product,
    permitio_resource.review,
  ]
}

resource "permitio_role" "product_vendor" {
  key         = "vendor"
  name        = "vendor"
  description = "Update and delete own products"
  resource    = permitio_resource.product.key
  permissions = ["product:update", "product:delete"]
  depends_on  = [
    permitio_resource.product,
  ]
}

resource "permitio_role" "review_moderator" {
  key         = "moderator"
  name        = "moderator"
  description = "Delete reviews on own products"
  resource    = permitio_resource.review.key
  permissions = ["review:delete"]
  depends_on  = [
    permitio_resource.review,
  ]
}

// Derive product#vendor to review#moderator for child reviews
resource "permitio_role_derivation" "product_vendor_review_moderator" {
  role        = permitio_role.product_vendor.key
  on_resource = permitio_resource.product.key
  resource    = permitio_resource.review.key
  to_role     = permitio_role.review_moderator.key
  linked_by   = permitio_relation.product_review_relation.key
  depends_on  = [
    permitio_resource.product,
    permitio_resource.review,
    permitio_role.review_moderator,
    permitio_role.product_vendor,
    permitio_relation.product_review_relation,
  ]
}

resource "permitio_role" "viewer" {
  key         = "viewer"
  name        = "viewer"
  description = "View and review on all products"
  permissions = ["product:read", "review:create"]
  depends_on  = [
    permitio_resource.product,
    permitio_resource.review,
  ]
}

resource "permitio_role" "editor" {
  key         = "editor"
  name        = "editor"
  description = "Add products, update and delete them, and delete reviews on them"
  permissions = ["product:read", "product:create", "product:update", "product:delete"]
  depends_on  = [
    permitio_resource.product,
  ]
}

resource "permitio_role" "admin" {
  key         = "admin"
  name        = "admin"
  description = "Delete any product or review"
  permissions = ["product:delete", "review:delete"]
  depends_on  = [
    permitio_resource.product,
    permitio_resource.review,
  ]
}

// Give 'editor' role permissions to 'own_product:update'
resource "permitio_condition_set_rule" "allow_editors_to_update_own_products" {
  user_set     = permitio_role.editor.key
  resource_set = permitio_resource_set.own_product.key
  permission   = "own_product:update"
  depends_on   = [
    permitio_resource_set.own_product,
  ]
}

// Give 'editor' role permissions to 'own_product:delete'
resource "permitio_condition_set_rule" "allow_editors_to_delete_own_products" {
  user_set     = permitio_role.editor.key
  resource_set = permitio_resource_set.own_product.key
  permission   = "own_product:delete"
  depends_on   = [
    permitio_resource_set.own_product,
  ]
}

// Give 'viewer' role permissions to 'own_review:update'
resource "permitio_condition_set_rule" "allow_viewers_to_update_own_reviews" {
  user_set     = permitio_role.viewer.key
  resource_set = permitio_resource_set.own_review.key
  permission   = "own_review:update"
  depends_on   = [
    permitio_resource_set.own_review,
  ]
}

// Give 'viewer' role permissions to 'own_review:delete'
resource "permitio_condition_set_rule" "allow_viewers_to_delete_own_reviews" {
  user_set     = permitio_role.viewer.key
  resource_set = permitio_resource_set.own_review.key
  permission   = "own_review:delete"
  depends_on   = [
    permitio_resource_set.own_review,
  ]
}
