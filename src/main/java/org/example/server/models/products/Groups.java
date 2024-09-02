package org.example.server.models.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "'GROUPS'")
public class Groups {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column (name = "id_group")
    private String idGroup;

    @Column(name = "parent_Group")
    private String parentGroup;

    @Column(name = "is_Deleted")
    private Boolean isDeleted;

    @Column(name = "name")
    private String name;

    @Column(name = "is_Included_In_Menu")
    private Boolean isIncludedInMenu;

    @Column(name = "is_Group_Modifier")
    private Boolean isGroupModifier;

    public Groups(String idGroup, String parentGroup,String name, Boolean isDeleted, Boolean isIncludedInMenu, Boolean isGroupModifier) {
        this.idGroup = idGroup;
        this.parentGroup = parentGroup;
        this.name = name;
        this.isDeleted = isDeleted;
        this.isIncludedInMenu = isIncludedInMenu;
        this.isGroupModifier = isGroupModifier;
    }

}
